package ru.fizteh.fivt.students.paulinMatavina.filemap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.DataFormatException;
import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.students.paulinMatavina.shell.ShellState;
import ru.fizteh.fivt.students.paulinMatavina.utils.*;

public class MultiDbState extends State implements Table {
    final int folderNum = 16;
    final int fileInFolderNum = 16;
    public String tableName;
    DbState[][] savedData;
    DbState[][] unsavedData;
    public ShellState shell;
    private String rootPath;
    public boolean isDropped;
    public int changesNum;
    private int dbSize;
    private int primaryDbSize;
    
    public MultiDbState(String property) throws IllegalArgumentException {
        if (property == null || property.isEmpty()) {
            throw new IllegalArgumentException("wrong root directory");
        }
        
        changesNum = 0;
        dbSize = 0;
        isDropped = false;
        rootPath = new File(property).getAbsolutePath();
        savedData = new DbState[folderNum][fileInFolderNum];
        unsavedData = new DbState[folderNum][fileInFolderNum];
        shell = new ShellState();
        shell.cd(rootPath);
        
        tableName = null;
        if (setCurrentDir() != 0) {
            throw new IllegalArgumentException(property + ": wrong root directory");
        }
        
        commands = new HashMap<String, Command>();
        this.add(new DbGet());
        this.add(new DbPut());
        this.add(new DbRemove());
        this.add(new MultiDbDrop());
        this.add(new MultiDbCreate());
        this.add(new MultiDbUse());
        this.add(new DbCommit());
        this.add(new DbRollback());
        this.add(new DbSize());
    }
    
    private int checkFolder(String path) {
        File f = new File(path);
        if (!f.exists()) {
            String[] args = {path};
            int result = shell.mkdir(args);
            return result;
        }
        
        if (!f.isDirectory()) {
            return 1;
        }
        return 0;
    }
    
    private void loadData() throws IOException, DataFormatException {
        dbSize = 0;
        savedData = new DbState[folderNum][fileInFolderNum];
        unsavedData = new DbState[folderNum][fileInFolderNum];
        for (int i = 0; i < folderNum; i++) {
            String fold = Integer.toString(i) + ".dir";
            if (checkFolder(shell.makeNewSource(fold)) != 0) {
                throw new DataFormatException("wrong subfolder " + i);
            }
            for (int j = 0; j < fileInFolderNum; j++) {
                String file = Integer.toString(j) + ".dat";
                String filePath = shell.makeNewSource(fold, file);
                savedData[i][j] = new DbState(filePath, i, j);
                File f = new File(savedData[i][j].path);
                f.createNewFile();
                dbSize += savedData[i][j].loadData();
            }
        }
        primaryDbSize = dbSize;
        closeAll();
    }
    
    public boolean fileExist(String name) {
        return new File(makeNewSource(name)).exists();
    }
    
    public boolean isDbChosen() {
        return tableName != null;
    }
    
    private int setCurrentDir() {
        currentDir = new File(rootPath);
        if (!currentDir.exists() || !currentDir.isDirectory()) {
            return 1;
        } else {
            shell.cd(rootPath);
            return 0;
        }
    }
    
    public int changeBase(String name) {
        if (isDbChosen()) {
            commit();   
        }
        dbSize = 0;
        changesNum = 0;
        tableName = name;
        isDropped = false;
        File lastDir = shell.currentDir;
        int result = shell.cd(makeNewSource(name));
        if (result == 0) {
            try {
                loadData();
            } catch (IOException e) {
                shell.currentDir = lastDir;
                System.err.println("multifilemap: loading data: " + e.getMessage());
                throw new IllegalArgumentException();
            } catch (DataFormatException e) {
                shell.currentDir = lastDir;
                System.err.println("multifilemap: " + e.getMessage());
                throw new IllegalArgumentException();
            }
        }
        
        return result;
    }
     
    @Override
    public int exitWithError(int errCode) throws DbExitException {
        if (!isDbChosen()) {
            throw new DbExitException(0);
        }
        int result = commit();
        if (result < 0) {
            errCode = 1;
        }
        
        throw new DbExitException(errCode);
    }
    
    public int commit() {
        try {
            return tryToCommit();
        } catch (IOException e) {
            System.out.println("multifilemap: error while writing data to the disk");
            return 0;
        } catch (DataFormatException e) {
            System.out.println("multifilemap: " + e.getMessage());
            return 0;
        }
    }
    
    public void closeAll() throws IOException {
        for (int i = 0; i < folderNum; i++) {
            for (int j = 0; j < fileInFolderNum; j++) {
                savedData[i][j].dbFile.close(); 
            }
        }
    }
    
    private int tryToCommit() throws IOException, DataFormatException {
        if (isDropped || !isDbChosen()) {
            return 0;
        }
        
        for (int i = 0; i < folderNum; i++) {
            String fold = Integer.toString(i) + ".dir";
            if (checkFolder(shell.makeNewSource(fold)) != 0) {
                throw new DataFormatException("wrong subfolder " + i);
            }
            for (int j = 0; j < fileInFolderNum; j++) {
                String file = Integer.toString(j) + ".dat";
                savedData[i][j].commit();
                if (savedData[i][j].data.isEmpty()) {
                    String[] arg = {shell.makeNewSource(fold, file)};
                    shell.rm(arg);
                }
            }
           
            if (new File(shell.makeNewSource(fold)).listFiles().length == 0) {
                String[] arg = {fold};
                shell.rm(arg);
            }
        }
        
        closeAll();
        int chNum = changesNum;
        primaryDbSize = dbSize;
        changesNum = 0;
        return chNum;
    }
    
    private int getFolderNum(String key) {
        byte[] bytes = key.getBytes();
        return (Math.abs(bytes[0]) % 16);
    }
    
    private int getFileNum(String key) {
        byte[] bytes = key.getBytes();
        return (Math.abs(bytes[0]) / 16 % 16);
    }
    
    public String put(String key, String value) { 
        if (key == null || value == null || key.trim().isEmpty() 
                || value.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }

        if (!isDbChosen() || isDropped) {
            return null;
        }
        
        int folder = getFolderNum(key);
        int file = getFileNum(key);
        String result = savedData[folder][file].put(new String[] {key, value});
        if (result == null) {
            changesNum++;
            dbSize++;
            unsavedData[folder][file].put(new String[] {key, value});
        }
        return result;  
    }
    
    public String get(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        
        if (!isDbChosen() || isDropped) {
            return null;
        }
        
        int folder = getFolderNum(key);
        int file = getFileNum(key);
        return savedData[folder][file].get(new String[] {key});  
    }
    
    public String remove(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (!isDbChosen() || isDropped) {
            return null;
        }
        
        int folder = getFolderNum(key);
        int file = getFileNum(key);
        String result = savedData[folder][file].remove(new String[] {key});
        if (result != null) {
            String res2 = unsavedData[folder][file].remove(new String[] {key});
            if (res2 == null) {
                changesNum++;
            }
            dbSize--;
            //changesNum++;
        }
        return result;  
    }
    
    public int size() {
        return dbSize;
    }
    
    public static boolean checkNameValidity(String dbName) {
        return !(dbName.contains("/") || dbName.contains("\\") || dbName.contains("?")
                || dbName.contains(".") || dbName.contains("*") 
                || dbName.contains(":") || dbName.contains("\""));
    }
    
    public int rollback() {
        int chNum = changesNum;
        changesNum = 0;
        try {
            loadData();
        } catch (DataFormatException e) {
            System.err.println("database: wrong format");
        } catch (IOException e) {
            System.err.println("database: wrong format");
        }   
        dbSize = primaryDbSize;
        return chNum;
    }
    
    public String getName() {
        return tableName;
    }
    
    public void use(String dbName) {
        if (dbName == null || dbName.trim() == null || dbName.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (!checkNameValidity(dbName)) {
            throw new RuntimeException("in use " + dbName);
        }
        if (!fileExist(dbName)) {
            throw new DbReturnStatus(2);
        }
        
        changeBase(dbName);
        throw new DbReturnStatus(0);
    }
    
    public void create(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }      
        
        if (fileExist(name)) {
            throw new DbReturnStatus(2);
        }
        
        name = makeNewSource(name);
        shell.mkdir(new String[] {name});
        throw new DbReturnStatus(0);
    }
}
