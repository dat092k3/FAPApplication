package com.example.fapapplication.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fapapplication.R;
import com.example.fapapplication.adapter.CurriculumAdapter;
import com.example.fapapplication.model.Curriculum;

import java.util.ArrayList;

public class CurriculumActivity extends AppCompatActivity {

    private RecyclerView rvCurriculum;
    private CurriculumAdapter adapter;
    private ArrayList<Curriculum> list = new ArrayList<>();
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curriculum);

        rvCurriculum = findViewById(R.id.rvCurriculum);
        btnBack = findViewById(R.id.btnBack);
        rvCurriculum.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Thêm dữ liệu thủ công (hardcode)
        loadData();

        adapter = new CurriculumAdapter(list);
        rvCurriculum.setAdapter(adapter);
        
        // Setup back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadData() {
        list.add(new Curriculum("1", "GDQP", "0"));
        list.add(new Curriculum("2", "VOV124", "0"));
        list.add(new Curriculum("3", "VOV134", "0"));
        list.add(new Curriculum("4", "VOV114", "0"));
        list.add(new Curriculum("5", "TRS601", "0"));
        list.add(new Curriculum("6", "TMI101", "0"));
        list.add(new Curriculum("7", "SSL101c", "1"));
        list.add(new Curriculum("8", "CSI104", "1"));
        list.add(new Curriculum("9", "PRF192", "1"));
        list.add(new Curriculum("10", "MAE101", "1"));
        list.add(new Curriculum("11", "CEA201", "1"));
        list.add(new Curriculum("12", "PRO192", "2"));
        list.add(new Curriculum("13", "MAD101", "2"));
        list.add(new Curriculum("14", "OSG202", "2"));
        list.add(new Curriculum("15", "SSG104", "2"));
        list.add(new Curriculum("16", "NWC203c", "2"));
        list.add(new Curriculum("17", "JPD113", "3"));
        list.add(new Curriculum("18", "CSD201", "3"));
        list.add(new Curriculum("19", "DBI202", "3"));
        list.add(new Curriculum("20", "LAB211", "3"));
        list.add(new Curriculum("21", "WED201c", "3"));
        list.add(new Curriculum("22", "MAS291", "4"));
        list.add(new Curriculum("23", "JPD123", "4"));
        list.add(new Curriculum("24", "IOT102", "4"));
        list.add(new Curriculum("25", "PRJ301", "4"));
        list.add(new Curriculum("26", "SWE201c", "4"));
        list.add(new Curriculum("27", "SWP391", "5"));
        list.add(new Curriculum("28", "ITE302c", "5"));
        list.add(new Curriculum("29", "WDU203c", "5"));
        list.add(new Curriculum("30", "SWR302", "5"));
        list.add(new Curriculum("31", "SWT301", "5"));
        list.add(new Curriculum("32", "ENW492c", "6"));
        list.add(new Curriculum("33", "OJT202", "6"));
        list.add(new Curriculum("34", "SYB302c", "7"));
        list.add(new Curriculum("35", "PMG201c", "7"));
        list.add(new Curriculum("36", "SWD392", "7"));
        list.add(new Curriculum("37", "KOR311", "7"));
        list.add(new Curriculum("38", "KOR321", "8"));
        list.add(new Curriculum("39", "KOR411", "8"));
        list.add(new Curriculum("40", "MLN111", "8"));
        list.add(new Curriculum("41", "MLN122", "8"));
        list.add(new Curriculum("42", "PRM392", "8"));
        list.add(new Curriculum("43", "HCM202", "9"));
        list.add(new Curriculum("44", "MLN131", "9"));
        list.add(new Curriculum("45", "VNR202", "9"));
        list.add(new Curriculum("46", "SE_GRA_ELE", "9"));
    }
}
