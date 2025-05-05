package com.dam.kairos.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

//import com.dam.kairos.ui.fragments.PerfilFragment;
import com.dam.kairos.R;
import com.dam.kairos.ui.fragments.AnalisisFragment;
import com.dam.kairos.ui.fragments.CalendarioFragment;
import com.dam.kairos.ui.fragments.DiarioFragment;
//import com.dam.kairos.ui.fragments.FeedFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Iniciando la actividad");

        // Inicializar vistas
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (viewPager == null || bottomNavigationView == null) {
            Log.e(TAG, "Error: No se pudieron encontrar las vistas");
        }

        // Configurar adaptador para el ViewPager2
        MyPagerAdapter adapter = new MyPagerAdapter(this);
        viewPager.setAdapter(adapter);

        Log.d(TAG, "Adaptador asignado al ViewPager2");

        // Verificar que el adaptador tiene elementos antes de establecer la página por defecto
        if (adapter.getItemCount() > 1) {
            viewPager.setCurrentItem(0,false);
            Log.d(TAG, "Página inicial establecida en el índice 1");
        } else {
            Log.e(TAG, "Error: El adaptador no tiene suficientes elementos");
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                Log.d(TAG, "Elemento seleccionado en BottomNavigationView: " + itemId);
                if (itemId == R.id.menu_diario) {
                    viewPager.setCurrentItem(0, false);
                    return true;
                } else if (itemId == R.id.menu_calendario) {
                    viewPager.setCurrentItem(1, false);
                    return true;
                //} else if (itemId == R.id.menu_feed) {
                    //viewPager.setCurrentItem(2, false);
                    //return true;
                } else if (itemId == R.id.menu_analisis) {
                    viewPager.setCurrentItem(2, false); //cambiar a 3 en el futuro TODO
                    return true;
                //} else if (itemId == R.id.menu_perfil) {
                    //viewPager.setCurrentItem(4, false);
                    //return true;
                } else {
                    Log.e(TAG, "Error: ID de menú desconocido");
                    return false;
                }
            }
        });

        // Sincronizar la selección del BottomNavigationView al deslizar el ViewPager2
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d(TAG, "Página seleccionada: " + position);
                switch (position){
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.menu_diario);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.menu_calendario);
                        break;
                    case 2:
                        // bottomNavigationView.setSelectedItemId(R.id.menu_feed);
                        // break;
                    //case 3:
                        bottomNavigationView.setSelectedItemId(R.id.menu_analisis);
                        break;
                    //case 4:
                        //bottomNavigationView.setSelectedItemId(R.id.menu_perfil);
                        //break;
                    default:
                        Log.e(TAG, "Error: Posición desconocida en ViewPager2");
                }
            }
        });
    }

    // Adaptador para el ViewPager2
    private static class MyPagerAdapter extends FragmentStateAdapter {

        public MyPagerAdapter(@NonNull AppCompatActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Log.d(TAG, "Creando fragmento para posición: " + position);
            switch (position){
                case 0:
                    return new DiarioFragment();
                case 1:
                    return new CalendarioFragment();
                case 2:
                    //return new FeedFragment();
                //case 3:
                    return new AnalisisFragment();
                //case 4:
                    //return new PerfilFragment();
                default:
                    Log.e(TAG, "Error: Posición inválida en createFragment: " + position);
                    throw new IllegalArgumentException("Posición inválida: " + position);
            }
        }

        @Override
        public int getItemCount() {
            return 3; //en el futuro deberan de ser 5 TODO
        }
    }
}
