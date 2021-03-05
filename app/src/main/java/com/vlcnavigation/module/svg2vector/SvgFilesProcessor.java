package com.vlcnavigation.module.svg2vector;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.android.ide.common.vectordrawable.Svg2Vector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import timber.log.Timber;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Credits to ravibhojwani86 @ https://github.com/ravibhojwani86/Svg2VectorAndroid
 * Created by ravi on 18/12/17.
 */

public class SvgFilesProcessor {

    private final Path sourceSvgPath;
    private final Path destinationVectorPath;
    private final String extension;
    private final String extensionSuffix;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public SvgFilesProcessor(String sourceSvgDirectory) {
        this(sourceSvgDirectory, sourceSvgDirectory+ File.pathSeparator + "ProcessedSVG", "xml", "");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public SvgFilesProcessor(String sourceSvgDirectory, String destinationVectorDirectory) {
        this(sourceSvgDirectory, destinationVectorDirectory, "xml", "");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public SvgFilesProcessor(String sourceSvgDirectory, String destinationVectorDirectory, String extension,
                             String extensionSuffix) {
        this.sourceSvgPath = Paths.get(sourceSvgDirectory);
        this.destinationVectorPath = Paths.get(destinationVectorDirectory);
        this.extension = extension;
        this.extensionSuffix = extensionSuffix;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void process(){
        try{
            EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            //check first if source is a directory
            if(Files.isDirectory(sourceSvgPath)){
                Files.walkFileTree(sourceSvgPath, options, Integer.MAX_VALUE, new FileVisitor<Path>() {

                    public FileVisitResult postVisitDirectory(Path dir,
                                                              IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    public FileVisitResult preVisitDirectory(Path dir,
                                                             BasicFileAttributes attrs)  {
                        // Skip folder which is processing svgs to xml
                        if(dir.equals(destinationVectorPath)){
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        CopyOption[] opt = new CopyOption[]{COPY_ATTRIBUTES, REPLACE_EXISTING};
                        Path newDirectory = destinationVectorPath.resolve(sourceSvgPath.relativize(dir));
                        try{
                            Files.copy(dir, newDirectory,opt);
                        } catch(FileAlreadyExistsException ex){
                            Timber.d("FileAlreadyExistsException: %s", ex.toString());
                        } catch(IOException x){
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return CONTINUE;
                    }


                    public FileVisitResult visitFile(Path file,
                                                     BasicFileAttributes attrs) throws IOException {
                        convertToVector(file, destinationVectorPath.resolve(sourceSvgPath.relativize(file)));
                        return CONTINUE;
                    }


                    public FileVisitResult visitFileFailed(Path file,
                                                           IOException exc) throws IOException {
                        return CONTINUE;
                    }
                });
            } else {
                Timber.d("source not a directory");
            }

        } catch (IOException e){
            Timber.d("IOException: %s", e.getMessage());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void convertToVector(Path source, Path target) throws IOException{
        // convert only if it is .svg
        if(source.getFileName().toString().endsWith(".svg")){
            File targetFile = getFileWithXMlExtension(target, extension, extensionSuffix);
            FileOutputStream fous = new FileOutputStream(targetFile);
            Svg2Vector.parseSvgToXml(source.toFile(), fous);
            Timber.d("Converted this file: %s", source.toFile().getAbsolutePath());
        } else {
            Timber.d("Skipping file as its not svg: %s", source.getFileName().toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private File getFileWithXMlExtension(Path target, String extension, String extensionSuffix){
        String svgFilePath =  target.toFile().getAbsolutePath();
        StringBuilder svgBaseFile = new StringBuilder();
        int index = svgFilePath.lastIndexOf(".");
        if(index != -1){
            String subStr = svgFilePath.substring(0, index);
            svgBaseFile.append(subStr);
        }
        svgBaseFile.append(null != extensionSuffix ? extensionSuffix : "");
        svgBaseFile.append(".");
        svgBaseFile.append(extension);
        return new File(svgBaseFile.toString());
    }

}