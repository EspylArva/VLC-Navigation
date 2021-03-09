package com.vlcnavigation.ui.fft;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.ide.common.vectordrawable.Svg2Vector;
import com.vlcnavigation.R;
import com.vlcnavigation.module.svg2vector.SvgFilesProcessor;
import com.vlcnavigation.module.trilateration.Trilateration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;


public class FFTFragment extends Fragment {

    private FFTViewModel FFTViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FFTViewModel =
                new ViewModelProvider(this).get(FFTViewModel.class);
        View root = inflater.inflate(R.layout.fragment_fft, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        FFTViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });


//        try {
//            parseBlueprint("svg_res", "svg");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



        return root;
    }


    /*
    SvgFilesProcessor processor = new SvgFilesProcessor("/Volumes/Development/Features/MySvgs");
    ==> sourceSvgPath = "/Volumes/Development/Features/MySvgs"
        destinationVectorDirectory = "/Volumes/Development/Features/MySvgs/ProcessedSVG"
        extension = "xml"
        extensionSuffix = ""

    processor.process()

    ==> convertToVector( file, destinationVectorPath.resolve(sourceSvgPath.relativize(file)) )
     */

    public void parseBlueprint(String blueprintFileName, String extension) throws IOException {
        // https://stackoverflow.com/questions/14376807/read-write-string-from-to-a-file-in-android
        File pathToInternal = getContext().getFilesDir();
        File pathToSd = getContext().getExternalFilesDir(null);

        String fileName = String.format("%s.%s", blueprintFileName, extension);
        String output = String.format("%s.%s", blueprintFileName, "xml");
        Timber.d("We got those two paths: << %s (Internal Space) >> & << %s (SD Card) >>", pathToInternal, pathToSd);

//        Timber.d("For now, we are only using SD Card. Locating %s in SD Card (%s)", fileName, pathToSd);
        File mySvg = new File(pathToSd, fileName);
        File myMap = new File(pathToSd, fileName);
        FileOutputStream fos = new FileOutputStream(myMap);
        Svg2Vector.parseSvgToXml(mySvg, fos);

        SvgFilesProcessor proc = new SvgFilesProcessor(pathToSd.toString(), pathToSd.toString(), "xml", "");
//        SvgFilesProcessor proc = new SvgFilesProcessor(pathToSd.toString());
//        proc.process();


        // https://github.com/ravibhojwani86/Svg2VectorAndroid/blob/master/src/main/java/com/vector/svg2vectorandroid/SvgFilesProcessor.java
        // https://android.googlesource.com/platform/tools/base/+/master/sdk-common/src/main/java/com/android/ide/common/vectordrawable/Svg2Vector.java
    }



}