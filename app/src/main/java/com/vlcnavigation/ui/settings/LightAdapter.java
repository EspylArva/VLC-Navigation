package com.vlcnavigation.ui.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.vlcnavigation.R;
import com.vlcnavigation.module.trilateration.Floor;
import com.vlcnavigation.module.trilateration.Light;
import com.vlcnavigation.module.utils.Util;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import timber.log.Timber;

//TODO: Javadoc

public class LightAdapter extends RecyclerView.Adapter<LightAdapter.LightHolder> {
    private final SettingsViewModel vm;

    public LightAdapter(SettingsViewModel vm) { this.vm = vm; }

    @NonNull
    @Override
    public LightHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_light_entry, parent, false);
        return new LightHolder(v, vm);
    }

    @Override
    public void onBindViewHolder(@NonNull LightHolder holder, int position) {
        // Refresh UI
        holder.setLight();
        holder.refreshUI();

        holder.test();
        //FIXME: Selecting other view resets the position of the recycler view
    }

    @Override
    public int getItemCount() {
        return vm.getListOfLights().getValue().size();
    }

    public class LightHolder extends RecyclerView.ViewHolder
    {
        private final SettingsViewModel vm;
        private Light light;

        private TextInputLayout txtInputDescription;
        private TextInputLayout txtInputLayout_posX, txtInputLayout_posY;
        private TextInputLayout txtInputLayout_lambda, txtInputLayout_distance;
        private AutoCompleteTextView txtInputLayout_floor;
        private AppCompatButton btn_deleteEntry;

        public LightHolder(@NonNull View itemView, SettingsViewModel vm) {
            super(itemView);
            this.vm = vm;
            initViews(itemView);        // Instantiate views
        }

        public void setLight() { this.light = vm.getListOfLights().getValue().get(getAdapterPosition()); }

        private void initViews(View itemView) {
            this.txtInputDescription = itemView.findViewById(R.id.txtInputLayout_light_description);
            this.txtInputLayout_posX = itemView.findViewById(R.id.txtInputLayout_light_x);
            this.txtInputLayout_posY = itemView.findViewById(R.id.txtInputLayout_light_y);
            this.txtInputLayout_floor = itemView.findViewById(R.id.txtInputLayout_light_floor);
            this.txtInputLayout_lambda = itemView.findViewById(R.id.txtInputLayout_light_lambda);
            this.txtInputLayout_distance = itemView.findViewById(R.id.txtInputLayout_light_distance);
            this.btn_deleteEntry = itemView.findViewById(R.id.btn_deleteEntry);
        }


        public void refreshUI() // Call this after setting the position
        {
            this.txtInputDescription.getEditText().setText(light.getDescription());
            this.txtInputLayout_posX.getEditText().setText(String.valueOf(light.getPosX()));
            this.txtInputLayout_posY.getEditText().setText(String.valueOf(light.getPosY()));
            this.txtInputLayout_lambda.getEditText().setText(String.valueOf(light.getLambda()));
            this.txtInputLayout_distance.getEditText().setText(String.valueOf(light.getDistance()));
            if(light.getFloor() != null)
            {
                this.txtInputLayout_floor.setText(String.valueOf(light.getFloor().getOrder()));
            }
        }

        public void test()
        {
            initOnClickListeners();       // Click listeners. Currently: 1 (Remove button)
            initTextChangeListener();     // When a text field is changed, we want to save it to SP
            initFloorMenuListener();
        }

        private void initOnClickListeners() {
            this.btn_deleteEntry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position > -1)
                    {
                        Util.hideKeyboardFromView(v);
                        vm.removeLightAt(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, vm.getListOfLights().getValue().size());
                    }
                }
            });
        }

        private void initTextChangeListener() {
            txtInputLayout_posX.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0 || s.toString().endsWith("-") || s.toString().endsWith("+") || s.toString().endsWith(".")) {
                        txtInputLayout_posX.setError(itemView.getContext().getResources().getString(R.string.light_x_null));
                        txtInputLayout_posX.setErrorEnabled(true);
                    }
                    else
                    {
                        txtInputLayout_posX.setErrorEnabled(false);
                        Timber.d("Adapter position: %s. Lights' size: %s. Adapter item count: %s",
                                getAdapterPosition(),
                                vm.getListOfLights().getValue().size(),
                                getItemCount()
                        );
                        vm.getListOfLights().getValue().get(getAdapterPosition()).setPosX(Double.parseDouble(s.toString()));
                        vm.saveLights();
                    }
                }
            });
            txtInputLayout_posY.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0 || s.toString().equals("-") || s.toString().endsWith("+") || s.toString().endsWith(".")) {
                        txtInputLayout_posY.setError(itemView.getContext().getResources().getString(R.string.light_y_null));
                        txtInputLayout_posY.setErrorEnabled(true);
                    }
                    else
                    {
                        txtInputLayout_posY.setErrorEnabled(false);
                        vm.getListOfLights().getValue().get(getAdapterPosition()).setPosY(Double.parseDouble(s.toString()));
                        vm.saveLights();
                    }
                }
            });
            txtInputLayout_lambda.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0 || s.toString().equals("-") || s.toString().endsWith("+") || s.toString().endsWith(".")) {
                        txtInputLayout_lambda.setError(itemView.getContext().getResources().getString(R.string.light_lambda_null));
                        txtInputLayout_lambda.setErrorEnabled(true);
                    }
                    else
                    {
                        txtInputLayout_lambda.setErrorEnabled(false);
                        vm.getListOfLights().getValue().get(getAdapterPosition()).setLambda(Double.parseDouble(s.toString()));
                        vm.saveLights();
                    }
                }
            });

            // CLEANME: not sure if it is useful, as Floor should be chosen using the dropdown list
//            txtInputLayout_floor.addTextChangedListener(new TextWatcher() {
//                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//                @Override public void afterTextChanged(Editable s) { }
//                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    if (s.length() == 0 || s.toString().equals("-") || s.toString().equals("+") || s.toString().equals(".")) {
//                        txtInputLayout_floor.setError(itemView.getContext().getResources().getString(R.string.light_floor_null));
////                        txtInputLayout_floor.setErrorEnabled(true);
//                    }
//                    else
//                    {
////                        txtInputLayout_floor.setErrorEnabled(false);
//                        vm.getListOfLights().getValue().get(getAdapterPosition()).getFloor().setDescription(s.toString());
//                        vm.saveLights();
//                    }
//                }
//            });

        }

        public void initFloorMenuListener()
        {
            if(!this.vm.floorExists(this.light.getFloor()))
            {
                Timber.d("Seems like this floor no longer exists: %s", this.light.getFloor());
                txtInputLayout_floor.setError(itemView.getResources().getString(R.string.floor_does_not_exist));
            }

            ListPopupWindow listPopupWindow = new ListPopupWindow(itemView.getContext(), null, R.attr.listPopupWindowStyle);
            listPopupWindow.setAnchorView(txtInputLayout_floor);

            ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(itemView.getContext(), R.layout.menu_layout_floor, vm.getListOfFloorLevels().getValue());
            listPopupWindow.setAdapter(adapter);

            listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int newFloorOrder = vm.getListOfFloorLevels().getValue().get(position);
                    vm.getListOfLights().getValue().get(getAdapterPosition()).getFloor().setOrder(newFloorOrder);
                    txtInputLayout_floor.setText(String.valueOf(newFloorOrder));
                    listPopupWindow.dismiss();
                }
            });
            txtInputLayout_floor.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { listPopupWindow.show(); Util.hideKeyboardFromView(v);} });
        }

    }


}
