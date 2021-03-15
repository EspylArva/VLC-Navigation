package com.vlcnavigation.ui.livemap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.vlcnavigation.R;
import com.vlcnavigation.components.DotIndicatorDecoration;
import com.vlcnavigation.ui.settings.LightAdapter;

public class LiveMapFragment extends Fragment {

//    private FloatingActionButton fab1, fab2, fab3;

    private LiveMapViewModel liveMapViewModel;
    private RecyclerView recycler_floors;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        liveMapViewModel = new ViewModelProvider(this).get(LiveMapViewModel.class);
        View root = initViews(inflater, container);
        initObservers();
        initListeners();

//        try{
//            Trilateration.triangulate();
//        } catch (Exception ex){ Timber.e(ex);}

        return root;
    }

    private void initListeners() {
//        fab1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    for(String svg : svgs)
//                    {
////                        Timber.w(svg);
//                        makeMap(svg);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        });
//
//        fab2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    String svg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                            "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
//                            "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"522px\" height=\"202px\" viewBox=\"-0.5 -0.5 522 202\" content=\"&lt;mxfile host=&quot;Electron&quot; modified=&quot;2021-03-09T10:38:23.073Z&quot; agent=&quot;Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) draw.io/11.3.0 Chrome/76.0.3809.139 Electron/6.0.7 Safari/537.36&quot; etag=&quot;4RtL0t270lhN2_pML5CD&quot; version=&quot;11.3.0&quot; type=&quot;device&quot; pages=&quot;1&quot;&gt;&lt;diagram id=&quot;ITolrCyJ6mziRDKKo1it&quot; name=&quot;Page-1&quot;&gt;3Zddr5sgGMc/jZdLVPDtdl17tmQnS06TnWsKjy8ZiqE42336oaLV1aY9J7VbdmPg/wDCj78PaKFVfniSpEyfBQNuuTY7WOiT5bp+FOhnIxw7AUVRJyQyY53knIRt9guMaBu1yhjsJw2VEFxl5VSkoiiAqolGpBT1tFks+PStJUngTNhSws/V14yptFNDNzjpnyFL0v7Njm/Wl5O+sVnJPiVM1CMJrS20kkKorpQfVsAbdj2Xrt/mQnSYmIRC3dJhvREvG/wNP5Xbr68oCV6+o+cPZpSfhFdmwWay6tgTkKIqGDSD2Bb6WKeZgm1JaBOt9ZZrLVU51zVHF+OM85XgQrZ9URzHLqVa3yspfsAowvyd7/k6YiYAUsHh4sqcgZf2GYgclDzqJn0H3yA2HsOmWo82zEjpaK96jRiLJMPAJ4q6YEC+Aaq7LFRGIIxnofo0hF18J6juFOpQH1HF9gxWvBTW8DpW/Y2VTTEnRUX4l6Ks1HW8+y7p4GYnpFBEZaLQ9cg+Rw8O8yCYQx/5ASJ38nP4B3n/L/s5uh08FbIAeZ15ez60xNuDIZxhHYcU5nPHLvSwZy/C+sbUMXwMd2fdn3tLuXyW9EOyNP7XbO0snKcfkywGy152sDuXppfDiv6HOwW+jvWxZsULU31ItvXcd1F9x5VCV09X6zY2+j9B698=&lt;/diagram&gt;&lt;/mxfile&gt;\" style=\"background-color: rgb(255, 255, 255);\"><defs/><g><rect x=\"40\" y=\"80\" width=\"400\" height=\"40\" fill=\"#dae8fc\" stroke=\"#6c8ebf\" pointer-events=\"none\"/></g></svg>";
//
//                    Timber.w(svg);
//
//
//                    makeMap(svg);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        fab3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    makeMap("");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    private void initObservers() { }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View root = inflater.inflate(R.layout.fragment_live_map, container, false);

        recycler_floors = root.findViewById(R.id.recycler_display_floors);

        recycler_floors.setHasFixedSize(true);
        //  Values
        FloorDisplayAdapter floorAdapter = new FloorDisplayAdapter(liveMapViewModel);
        recycler_floors.setAdapter(floorAdapter);
        // Orientation
        LinearLayoutManager recycler_layout = new LinearLayoutManager(getContext());
        recycler_layout.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_floors.setLayoutManager(recycler_layout);
        // Margin between items
        // recycler_lights.addItemDecoration(new RecyclerViewMargin(4, 1));
        // Item position
        /* FIXME : Will need to tweak the indicator to display on the side rather than at the bottom
            Probably want to display the floor name/order too */
        recycler_floors.addItemDecoration(new DotIndicatorDecoration());
        // Snapping on a viewholder
        SnapHelper snap = new PagerSnapHelper();
        snap.attachToRecyclerView(recycler_floors);

        return root;
    }
}