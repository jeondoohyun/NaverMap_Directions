package com.bnctech.testmap;

import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.bnctech.testmap.hmacsha256.Hmac;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
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

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class MapFragmentActivity extends AppCompatActivity implements OnMapReadyCallback {

    private NaverMap naver_m;
    private FusedLocationSource mLocationSource;
    private UiSettings mUiSettings;
    private String str;
    private TextView tv;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 최적의 위치를 반환함, 사용자의 현재 위치를 구할때 위치 정보
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

        // onMapReady()메소드를 실행시킨다.
        mapFragment.getMapAsync(this);

        tv = findViewById(R.id.tv_lati);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mLocationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!mLocationSource.isActivated()) { // 권한 거부됨
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
        // NaverMap 객체는 개발자가 마음대로 생성할수 없다, onMapReady()메소드 매개변수 에서만 생성됨
        naver_m = naverMap;
        ArrayList<InfoWindow> arrayInfo = new ArrayList<>();

        mUiSettings = naver_m.getUiSettings();
        
        // 현재위치 버튼 활성화
        mUiSettings.setLocationButtonEnabled(true);

        // 회전 제스처(베어링 각도조절) 비활성화
        mUiSettings.setRotateGesturesEnabled(false);

        // .setLocationSource()메소드 등록 해야 현재 위치 버튼 눌렀을때 추적 가능
        naver_m.setLocationSource(mLocationSource);

        // .setLocationTrackingMode() 위치 추적하는 명령문(ex 임의의 버튼을 만들어서 사용할때)
        naver_m.setLocationTrackingMode(LocationTrackingMode.Follow);

        // 센터 마커
        Marker marker1 = new Marker();
        naver_m.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int i, boolean b) {
                CameraPosition cameraPosition = naverMap.getCameraPosition();

                marker1.setPosition(new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude));
                marker1.setIconTintColor(Color.argb(255,0,0,0));
                marker1.setMap(naverMap);
                tv.setText("위도 : "+cameraPosition.target.latitude+"\n경도 : "+cameraPosition.target.longitude);
            }
        });


        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(new LatLng(38.694661, 122.464296))
                .include(new LatLng(38.694661, 131.393632))
                .include(new LatLng(32.877702, 131.393632))
                .include(new LatLng(32.877702, 122.464296))
                .build();

        // 지도가 특정 범위를 넘어가지 않도록 설정
        naver_m.setExtent(bounds);

        // 폴리오버레이 범위를 엄청 크게 지정 한다음 내부홀을 지정하여 투명도를 설정한다.(내부홀은 스타일 효과가 무효화 된다, 내부홀만 선명하게 보이고 바깥 부분은 회색으로 보인다.)
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

        new Task().execute();


        // AsyncTask를 이용한 https 통신
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
                    // .setRequestProperty() 헤더추가 메소드
                    conn.setRequestProperty("content-type", "application/json; charset=utf-8"); // charset 추가해서 에러 고쳤음, 원래는 utf=8만 썼었음
                    conn.setRequestMethod("POST");


                    // TIMESTAMP 헤더, 암호 헤더 추가
                    String time = System.currentTimeMillis() + "";
                    conn.setRequestProperty("bx-timestamp-v1", time);

                    Hmac hmac = new Hmac(time);
                    String passHeader = hmac.hget();
                    conn.setRequestProperty("bx-signature-v1", passHeader);

                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    // POST방식으로 json 데이터를 전송하고 서버로부터 InputStreamReader로 json데이터 값을 응답받는다.
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    String query = "{\"req_msg\":1}";   // {"req_msg":1}

                    wr.write(query, 0, query.length());
                    wr.flush();
                    wr.close();

                    // request(요청)를 ouputstream으로 한후 responsecode가 200으로 정상 리스폰 되었다면
                    // output으로 데이터 전송하고 flush, close해주고 input으로 response받아준다.
                    if (conn.getResponseCode() == conn.HTTP_OK) {   //conn.HTTP_OK 는 상수 200

                        InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                        BufferedReader reader = new BufferedReader(tmp);    // http응답이 reader에 담겨 있다.
                        StringBuffer buffer_in = new StringBuffer();
                        while ((str = reader.readLine()) != null) {
                            buffer_in.append(str);
                        }
                        receiveMsg = buffer_in.toString();
                        Log.e("receiveMsg-1", receiveMsg);

                        reader.close();

                        // 받은 json형식의 문자열중 마지막에 SUCCESS를 삭제후 GSON으로 파싱할것, 마지막 SUCCESS 문자때문에 Json형식이 깨져서 파싱이 불가하기 때문 , .replace()후 리턴받아야함(원본값이 바뀌는것이 아님)
                        receiveMsg = receiveMsg.replace("SUCCESS", "");

                        return receiveMsg;

                    } else {
                        Log.i("통신 결과", conn.getResponseCode() + "에러");
                        return receiveMsg;
                    }
                } catch (IOException e) {
                    Log.e("error", "아웃풋스트림 에러 : " + e.toString());
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

                            // marker 설정
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

                            //정보창 설정
                            arrayInfo.add(new InfoWindow());



                            //정보창 어댑터
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
                                    // 현재 마커에 정보 창이 열려있지 않을 경우 엶
                                    arrayInfo.get(finalI).open(marker);
                                } else {
                                    // 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                                    arrayInfo.get(finalI).close();
                                }

                                return true;
                            };

                            // 마커 클릭시 리스너 발동
                            marker.setOnClickListener(listener);

                            // 지도를 클릭하면 정보창이 닫힘
                            int finalI1 = i;
                            Log.e("finalI1",i+"");
                            naver_m.setOnMapClickListener(new NaverMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                                    // 마커가 아닌 지도를 눌렀을때 모든 정보창이 꺼지게끔 반복문을 돌려 모든 정보창을 .close()한다.
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

    class Task extends AsyncTask<Void, Integer, String> {
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
                String appDirectionUrl = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start="+bugae+"&goal="+gasan+"&option="+opt;
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

                Log.e("HTTPS 응답코드",responseCode+"");
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
                    Log.e("coor0", coor[0]+""); // 경도, double로 변환해서 오버레이 적용시키면됨
                    Log.e("coor1", coor[1]+""); // 위도

                    latLngs.add(new LatLng(Double.parseDouble(coor[1]), Double.parseDouble(coor[0])));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PathOverlay path = new PathOverlay();
                        path.setCoords(latLngs);
//                        Log.e("latLngs 데이터",latLngs.get(0).latitude+", "+latLngs.get(0).longitude+"\n"+latLngs.get(1).latitude+", "+latLngs.get(1).longitude);
                        path.setMap(naver_m);
                    }
                });


                Log.e("traoptimal 길이",jsonArray_trafast.length()+"");
                Log.e("path 길이",jsonArray_path.length()+"");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
