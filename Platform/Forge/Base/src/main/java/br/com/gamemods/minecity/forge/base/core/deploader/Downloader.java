package br.com.gamemods.minecity.forge.base.core.deploader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.function.Function;

public class Downloader
{
    private final ByteBuffer downloadBuffer = ByteBuffer.allocateDirect(1 << 23);
    private File destinationFolder;
    private Function<String, Comparable> comparableVersion;

    public Downloader(File destinationFolder, Function<String, Comparable> comparableVersion)
    {
        this.destinationFolder = destinationFolder;
        this.comparableVersion = comparableVersion;
        if(!this.destinationFolder.exists() && !this.destinationFolder.mkdirs())
            System.err.println("[MineCity] Failed to create dir: "+this.destinationFolder);
    }

    public void load(Dependency dep)
    {
        if(!checkExisting(dep))
        {
            download(dep);
        }
    }

    // Returns true if dep exists, false if it doesn't
    private boolean checkExisting(Dependency dep)
    {
        VersionedFile vFile;
        File[] files = destinationFolder.listFiles();
        if(files == null) return false;

        for(File f : files)
        {
            vFile = new VersionedFile(f.getName(), dep.getFile().getPattern(), comparableVersion);

            if(!vFile.matches() || !vFile.getName().equals(dep.getName()))
                continue;

            int cmp = vFile.getVersion().compareTo(dep.getVersion());
            if(cmp < 0)
            {
                System.out.println("[MineCity] Deleted old version " + f.getName());
                deleteDep(f);
                return false;
            }
            if(cmp > 0)
            {
                System.out.println(
                        "[MineCity] Warning: version of " + dep.getName() + ", " + vFile.getVersion() +
                                " is newer than request " + dep.getVersion());
            }

            return true;
        }

        return false;
    }

    private void deleteDep(File depFile)
    {
        if(!depFile.delete())
        {
            depFile.deleteOnExit();
            System.out.println("[MineCity] Was not able to delete file " + depFile.getPath() +
                    ". Will try to delete on exit.");
            System.exit(1);
        }
    }

    private void download(Dependency dep)
    {
        File depFile = new File(destinationFolder, dep.getFile().getFilename());

        try
        {
            URL depURL = new URL(dep.getUrl() + '/' + dep.getFile().getFilename());
            System.out.println("Downloading file " + depURL.toString());
            URLConnection connection = depURL.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "MineCity Downloader");
            int sizeGuess = connection.getContentLength();
            download(connection.getInputStream(), sizeGuess, depFile);
            System.out.println("Download Complete");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void download(InputStream is, int sizeGuess, File target) throws Exception
    {
        if(sizeGuess > downloadBuffer.capacity())
            throw new Exception(String.format("The file %s is too large to be downloaded - the download is invalid",
                    target.getName()
            ));

        downloadBuffer.clear();

        int bytesRead, fullLength = 0;

        try
        {
            byte[] smallBuffer = new byte[1024];
            while((bytesRead = is.read(smallBuffer)) >= 0)
            {
                downloadBuffer.put(smallBuffer, 0, bytesRead);
                fullLength += bytesRead;
            }
            is.close();
            downloadBuffer.limit(fullLength);
            downloadBuffer.position(0);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        if(!target.exists() && !target.createNewFile())
            System.err.println("[MineCity] Filed to create file: "+target);

        downloadBuffer.position(0);
        FileOutputStream fos = new FileOutputStream(target);
        fos.getChannel().write(downloadBuffer);
        fos.close();
    }
}