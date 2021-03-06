package com.bnctech.testmap;

import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.bnctech.testmap.hmacsha256.Hmac;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MapFragmentActivity extends AppCompatActivity implements OnMapReadyCallback {

    private NaverMap naver_m;
    private FusedLocationSource mLocationSource;
    private UiSettings mUiSettings;
    private String str;
    private TextView tv;

    Marker mMarker;

    private double my_lati;
    private double my_long;
    private double des_lati;
    private double des_long;

    PathOverlay path;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ????????? ????????? ?????????, ???????????? ?????? ????????? ????????? ?????? ??????
        mLocationSource = new FusedLocationSource(this, 101);

//        LatLng latLng = new LatLng(mLocationSource.getLastLocation().getLatitude(),
//                mLocationSource.getLastLocation().getLongitude());
//        CameraPosition cameraPosition = new CameraPosition(latLng);
//        NaverMapOptions options = new NaverMapOptions()
//                .camera(cameraPosition);

//        Log.e("null?",mLocationSource.getLastLocation().getLatitude()+"");

//        CameraPosition cameraPosition =
//                new CameraPosition(new LatLng(mLocationSource.getLastLocation().getLatitude(),
//                                                mLocationSource.getLastLocation().getLongitude()), 16);
//        NaverMapOptions options = new NaverMapOptions()
//                .camera(cameraPosition);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) ((FragmentManager) fm).findFragmentById(R.id.fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance(null);
            fm.beginTransaction().add(R.id.fragment, mapFragment).commit();
        }

        // onMapReady()???????????? ???????????????.
        mapFragment.getMapAsync(this);

        tv = findViewById(R.id.tv_lati);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mLocationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!mLocationSource.isActivated()) { // ?????? ?????????
                naver_m.setLocationTrackingMode(LocationTrackingMode.None);
            } else {
                naver_m.setLocationTrackingMode(LocationTrackingMode.NoFollow);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }


    public void move(View view) {
        CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(new LatLng(37.606204, 126.922839), 14);
        cameraUpdate.reason(3);
        cameraUpdate.animate(CameraAnimation.Linear, 700);
        naver_m.moveCamera(cameraUpdate);
    }




    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // NaverMap ????????? ???????????? ???????????? ???????????? ??????, onMapReady()????????? ???????????? ????????? ?????????
        naver_m = naverMap;
        ArrayList<InfoWindow> arrayInfo = new ArrayList<>();

        mUiSettings = naver_m.getUiSettings();
        
        // ???????????? ?????? ?????????
        mUiSettings.setLocationButtonEnabled(true);

        // ?????? ?????????(????????? ????????????) ????????????
        mUiSettings.setRotateGesturesEnabled(false);

        // .setLocationSource()????????? ?????? ?????? ?????? ?????? ?????? ???????????? ?????? ??????
        naver_m.setLocationSource(mLocationSource);

        // .setLocationTrackingMode() ?????? ???????????? ?????????(ex ????????? ????????? ???????????? ????????????)
        naver_m.setLocationTrackingMode(LocationTrackingMode.Follow);

        // ?????? ??????
        Marker marker1 = new Marker();
//        naver_m.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
//            @Override
//            public void onCameraChange(int i, boolean b) {
//                CameraPosition cameraPosition = naverMap.getCameraPosition();
//
//                marker1.setPosition(new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude));
//                marker1.setIconTintColor(Color.argb(255,0,0,0));
//                marker1.setMap(naverMap);
//                tv.setText("?????? : "+cameraPosition.target.latitude+"\n?????? : "+cameraPosition.target.longitude);
//            }
//        });


        InfoWindow naviInfo = new InfoWindow();
        naviInfo.setAdapter(new InfoWindow.DefaultTextAdapter(this) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                return "??? ?????????\n????????? ??????";

                // todo : ????????? ???????????? ????????? ????????? ????????? ????????? ?????? ????????? ??????
            }
        });
        
        naviInfo.setOnClickListener(new Overlay.OnClickListener() {
            @Override
            public boolean onClick(@NonNull Overlay overlay) {
//                Toast.makeText(MapFragmentActivity.this, "??????", Toast.LENGTH_SHORT).show();
                if (path != null) {
                    path.setMap(null);
                }
                new NaverNaviApi().execute();
                return false;
            }
        });

        naver_m.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                tv.setText("?????? : "+location.getLatitude()+"\n?????? : "+location.getLongitude());
                my_lati = location.getLatitude();
                my_long = location.getLongitude();
            }
        });

        naver_m.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
//                tv.setText("?????? : "+latLng.latitude+"\n?????? : "+latLng.longitude);

                if (mMarker == null) {
                    mMarker = new Marker();
                    mMarker.setPosition(new LatLng(latLng.latitude, latLng.longitude));
                    mMarker.setMap(naver_m);
                    naviInfo.open(mMarker);
                    des_lati = latLng.latitude;
                    des_long = latLng.longitude;
                } else {
                    naviInfo.close();
                    mMarker.setMap(null);
                    mMarker = null;
                }

            }
        });

//        Overlay.OnClickListener listener = overlay -> {
//            Marker marker = (Marker)overlay;
//
//            if (marker.getInfoWindow() == null) {
//                // ?????? ????????? ?????? ?????? ???????????? ?????? ?????? ???
//                infoWindow.open(marker);
//            } else {
//                // ?????? ?????? ????????? ?????? ?????? ???????????? ?????? ??????
//                infoWindow.close();
//            }
//
//            return true;
//        };


        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(new LatLng(38.694661, 122.464296))
                .include(new LatLng(38.694661, 131.393632))
                .include(new LatLng(32.877702, 131.393632))
                .include(new LatLng(32.877702, 122.464296))
                .build();

        // ????????? ?????? ????????? ???????????? ????????? ??????
        naver_m.setExtent(bounds);

//        naver_m.addOnLocationChangeListener();

        // ?????????????????? ????????? ?????? ?????? ?????? ????????? ???????????? ???????????? ???????????? ????????????.(???????????? ????????? ????????? ????????? ??????, ???????????? ???????????? ????????? ?????? ????????? ???????????? ?????????.)
        PolygonOverlay polygonOverlay = new PolygonOverlay();
        polygonOverlay.setCoords(Arrays.asList(
                new LatLng(38.53734, 125.69247),
                new LatLng(32.47275, 125.34095),
                new LatLng(35.24461, 130.58072),
                new LatLng(39.57769, 128.94398)
        ));

        polygonOverlay.setHoles(Collections.singletonList(Arrays.asList(
                new LatLng(37.642952, 126.913726),
                new LatLng(37.618387, 126.936593),
                new LatLng(37.598212, 126.928329),
                new LatLng(37.618722, 126.902925)
        )));

        polygonOverlay.setColor(Color.argb(50, 0, 0, 0));
        polygonOverlay.setOutlineWidth(7);
        polygonOverlay.setOutlineColor(Color.BLACK);
        polygonOverlay.setMap(naver_m);

//        new NaverNaviApi().execute();


        // AsyncTask??? ????????? https ??????
        new AsyncTask<String, Integer, String>() {

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected String doInBackground(String... strings) {
                URL url = null;
                String receiveMsg = "";
                try {
                    url = new URL("https://bnc-iot.com:33333/app/DALONG");

                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    // .setRequestProperty() ???????????? ?????????
                    conn.setRequestProperty("content-type", "application/json; charset=utf-8"); // charset ???????????? ?????? ?????????, ????????? utf=8??? ?????????
                    conn.setRequestMethod("POST");


                    // TIMESTAMP ??????, ?????? ?????? ??????
                    String time = System.currentTimeMillis() + "";
                    conn.setRequestProperty("bx-timestamp-v1", time);

                    Hmac hmac = new Hmac(time);
                    String passHeader = hmac.hget();
                    conn.setRequestProperty("bx-signature-v1", passHeader);

                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    // POST???????????? json ???????????? ???????????? ??????????????? InputStreamReader??? json????????? ?????? ???????????????.
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    String query = "{\"req_msg\":1}";   // {"req_msg":1}

                    wr.write(query, 0, query.length());
                    wr.flush();
                    wr.close();

                    // request(??????)??? ouputstream?????? ?????? responsecode??? 200?????? ?????? ????????? ????????????
                    // output?????? ????????? ???????????? flush, close????????? input?????? response????????????.
                    if (conn.getResponseCode() == conn.HTTP_OK) {   //conn.HTTP_OK ??? ?????? 200

                        InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                        BufferedReader reader = new BufferedReader(tmp);    // http????????? reader??? ?????? ??????.
                        StringBuffer buffer_in = new StringBuffer();
                        while ((str = reader.readLine()) != null) {
                            buffer_in.append(str);
                        }
                        receiveMsg = buffer_in.toString();
                        Log.e("receiveMsg-1", receiveMsg);

                        reader.close();

                        // ?????? json????????? ???????????? ???????????? SUCCESS??? ????????? GSON?????? ????????????, ????????? SUCCESS ??????????????? Json????????? ????????? ????????? ???????????? ?????? , .replace()??? ??????????????????(???????????? ??????????????? ??????)
                        receiveMsg = receiveMsg.replace("SUCCESS", "");

                        return receiveMsg;

                    } else {
                        Log.i("?????? ??????", conn.getResponseCode() + "??????");
                        return receiveMsg;
                    }
                } catch (IOException e) {
                    Log.e("error", "?????????????????? ?????? : " + e.toString());
                    return receiveMsg;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                StringBuffer buffer = new StringBuffer();
                if (result!=null){

                    try {
                        Gson gson = new GsonBuilder().setLenient().create();
                        Log.e("receiveMsg-2",result);
                        VO vo = gson.fromJson(result, VO.class);
                        ArrayList<VO.member> items = vo.device;

                        for (int i=0 ; i<items.size() ; i++){

                            // marker ??????
                            Marker marker = new Marker();
                            marker.setPosition(new LatLng(Double.parseDouble(items.get(i).last_latitude),
                                                            Double.parseDouble(items.get(i).last_longitude)));
                            marker.setIcon(MarkerIcons.GRAY);
                            if (Double.parseDouble(items.get(i).last_bike_battery)<=20){
                                marker.setIconTintColor(Color.argb(255,255,0,0));
                            }else {
                                marker.setIconTintColor(Color.argb(255,100,155,100));
                            }
                            marker.setZIndex(i);
                            String id = items.get(i).device_id;
                            String battery = items.get(i).last_bike_battery;
                            marker.setCaptionText(id.substring(id.length()-5,id.length()));
                            marker.setMap(naver_m);

                            //????????? ??????
                            arrayInfo.add(new InfoWindow());



                            //????????? ?????????
                            arrayInfo.get(i).setAdapter(new InfoWindow.DefaultTextAdapter(MapFragmentActivity.this) {
                                @NonNull
                                @Override
                                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                                    return "device_id : "+id+"\nbike_battery : "+battery;
                                }
                            });

//

                            int finalI = i;
                            Overlay.OnClickListener listener = overlay -> {

                                if (marker.getInfoWindow() == null) {
                                    // ?????? ????????? ?????? ?????? ???????????? ?????? ?????? ???
                                    arrayInfo.get(finalI).open(marker);
                                } else {
                                    // ?????? ?????? ????????? ?????? ?????? ???????????? ?????? ??????
                                    arrayInfo.get(finalI).close();
                                }

                                return true;
                            };

                            // ?????? ????????? ????????? ??????
                            marker.setOnClickListener(listener);

                            // ????????? ???????????? ???????????? ??????
                            int finalI1 = i;
                            Log.e("finalI1",i+"");
                            naver_m.setOnMapClickListener(new NaverMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                                    // ????????? ?????? ????????? ???????????? ?????? ???????????? ???????????? ???????????? ?????? ?????? ???????????? .close()??????.
                                    for (InfoWindow e : arrayInfo){
                                        e.close();
                                    }
                                }
                            });
                        }

//                        Log.e("data",buffer.toString());

                    }catch (Exception e){

                    }

                }
            }
        }.execute();
    }

    class NaverNaviApi extends AsyncTask<Void, Integer, String> {
        String gasan = "126.878202,37.482762";
        String seongnam = "127.128355,37.446311";
        String bugae = "126.740801,37.488466";
        String opt = "traavoidtoll:traavoidcaronly";

        StringBuffer response;

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String start = URLEncoder.encode(gasan,"UTF-8");
                String goal = URLEncoder.encode(seongnam,"UTF-8");
//                String appDirectionUrl = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start="+bugae+"&goal="+gasan+"&option="+opt;
                String appDirectionUrl = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start="+my_long+","+my_lati+"&goal="+des_long+","+des_lati+"&option="+opt;
                URL url = new URL(appDirectionUrl);
                HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();

                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("X-NCP-APIGW-API-KEY-ID","rjb8hwswvo");
                urlConn.setRequestProperty("X-NCP-APIGW-API-KEY","zPX7dreRE7xwVFK7EuQloadG4HkmpCZVmL8Mgfgj");
                urlConn.connect();

                int responseCode = urlConn.getResponseCode();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String inputLine;
                response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Log.e("HTTPS ????????????",responseCode+"");
                Log.e("HTTPS body", response.toString()+"");


            } catch (Exception e) {
                e.printStackTrace();
            }

            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject json_route = jsonObject.getJSONObject("route");
                JSONArray jsonArray_trafast = json_route.getJSONArray("traavoidtoll");
                JSONObject JSONObject_sub = jsonArray_trafast.getJSONObject(0);
                JSONArray jsonArray_path = JSONObject_sub.getJSONArray("path");

                List<LatLng> latLngs = new ArrayList<>();
                for (int i=0 ; i<jsonArray_path.length() ; i++) {
                    String[] coor = jsonArray_path.get(i).toString().substring(1,jsonArray_path.get(i).toString().length()-1).split(",");
                    Log.e("coor0", coor[0]+""); // ??????, double??? ???????????? ???????????? ??????????????????
                    Log.e("coor1", coor[1]+""); // ??????

                    latLngs.add(new LatLng(Double.parseDouble(coor[1]), Double.parseDouble(coor[0])));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        path = new PathOverlay();
                        path.setCoords(latLngs);
//                        Log.e("latLngs ?????????",latLngs.get(0).latitude+", "+latLngs.get(0).longitude+"\n"+latLngs.get(1).latitude+", "+latLngs.get(1).longitude);
                        path.setMap(naver_m);
                    }
                });


                Log.e("traoptimal ??????",jsonArray_trafast.length()+"");
                Log.e("path ??????",jsonArray_path.length()+"");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
