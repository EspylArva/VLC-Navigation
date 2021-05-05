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

//import com.android.ide.common.vectordrawable.Svg2Vector;
import com.vlcnavigation.R;

//csv reading
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;




import timber.log.Timber;


public class FFTFragment extends Fragment {

    private FFTViewModel FFTViewModel;
    //private List<String> rssData = new ArrayList<String>();
    private double[][] rssData;
    private double[][] distancesArray;



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

                //parse data from csv
                dataParser();
                fftComputing();

            }
        });

        return root;
    }



    public void dataParser(){

        InputStream inputStream = getResources().openRawResource(R.raw.powerratio2);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        int i = 0;

        try (

                CSVParser csvParser = new CSVParser(br, CSVFormat.DEFAULT
                        .withHeader("Tx1", "Tx2", "Tx3", "Tx4")
                        .withIgnoreHeaderCase()
                        .withSkipHeaderRecord()
                        .withTrim());
        ) {

            System.out.println("record number : "+Math.toIntExact(csvParser.getRecordNumber()));
            //rssData = new double[Math.toIntExact(csvParser.getRecordNumber())][4];
            rssData = new double[5][4];



            for (CSVRecord csvRecord : csvParser) {
                // Accessing values by the names assigned to each column
                Double Tx1 = Double.valueOf(csvRecord.get("Tx1"));
                Double Tx2 = Double.valueOf(csvRecord.get("Tx2"));
                Double Tx3 = Double.valueOf(csvRecord.get("Tx3"));
                Double Tx4 = Double.valueOf(csvRecord.get("Tx4"));

                System.out.println("---------------");
                System.out.println("Line number - " + csvRecord.getRecordNumber());

                System.out.println("Tx1 : " + Tx1);
                System.out.println("Tx2 : " + Tx2);
                System.out.println("Tx3 : " + Tx3);
                System.out.println("Tx4 : " + Tx4);


                rssData[i][0]=Tx1;
                rssData[i][1]=Tx2;
                rssData[i][2]=Tx3;
                rssData[i][3]=Tx4;
                i++;




            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("-------LISTE RSS--------\n\n");
        System.out.println(Arrays.deepToString(rssData));

    }

    public void fftComputing() {

        distancesArray = new double[5][4];


        double m, P, Ts, H, A, Dx1, Dx2, Dx3, Dx4;
        A = 1;
        m = 1;
        H = 1;

        for(int i=0; i<rssData.length; i++)
        {
            for(int j=0; j<rssData[i].length; j++)
            {
                System.out.println("-------Distance--------");
                distancesArray[i][j] = Math.pow( rssData[i][j] * ((A * (m+1) * Math.pow(H,m+1)) / 2 * Math.PI) , 1 / m+3 );
                System.out.println(distancesArray[i][j]);
            }
        }

        //Dx1 = Math.pow( Tx1 * ((A * (m+1) * Math.pow(H,m+1)) / 2 * Math.PI) , 1 / m+3 );



        System.out.println("-------LISTE Distances--------\n\n");
        System.out.println(Arrays.deepToString(distancesArray));


    }
}