package ru.fizteh.fivt.students.annasavinova.filemap.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ru.fizteh.fivt.students.annasavinova.filemap.DBaseProviderFactory;

public class DBaseProviderFactoryTest {
    DBaseProviderFactory test;

    @Rule
    public TemporaryFolder root = new TemporaryFolder();

    @Before
    public void init() throws IOException {
        System.setProperty("fizteh.db.dir", root.newFolder().getAbsolutePath());
        test = new DBaseProviderFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNull() throws IOException {
        test.create(null);
    }

    @Test
    public void testCreateNotExisting() throws IOException {
        test.create(root.newFolder().getAbsolutePath() + "not_existing_name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFile() throws IOException {
        test.create(root.newFile().getAbsolutePath());
    }

    @Test
    public void testCreate() throws IOException {
        assertNotNull(test.create(root.newFolder().getAbsolutePath()));

    }
}
