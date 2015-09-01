package tuannguyen.septatracker;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity {

    ArrayList<String> stations = new ArrayList<>();
    ArrayList<String> trains = new ArrayList<>();
    HashMap<String, String[]> trainLocations = new HashMap<>();
    String source;
    String dest;
    private GoogleMap map;
    final LatLng CENTER = new LatLng(39.9522222,-75.1641667);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new getStations().execute();
        View search_button = (Button)findViewById(R.id.search_trains_button);
        search_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                source = ((AutoCompleteTextView) findViewById(R.id
                        .auto_complete_list1)).getText().toString();
                dest = ((AutoCompleteTextView) findViewById(R.id
                        .auto_complete_list2)).getText().toString();
                String tempsource = source.replace(" ", "%20");
                String tempdest = dest.replace(" ", "%20");
                trains = new ArrayList<>();
                new getTrainList().execute("http://www3.septa.org/hackathon/TrainView/");
            }
        });

        ListView listview = (ListView)findViewById(R.id.trainListView);
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                           long id) {
                if(position > 0){
                    String trainnumber = ((TextView)view.findViewById(R.id.trainViewNo))
                            .getText().toString();
                    new getTrainInfo().execute("http://www3.septa.org/hackathon/RRSchedules/" +
                            trainnumber);
                }
                return true;
            }
        });
        View map_button = (Button)findViewById(R.id.map_button);
        map_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setContentView(R.layout.map_layout);
                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

                for(String train: trainLocations.keySet())
                {
                    String[] values = trainLocations.get(train);
                    LatLng location = new LatLng(Double.parseDouble(values[0]), Double.parseDouble(values[1]));
                    map.addMarker(new MarkerOptions().position(location)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action)));
                }

                // Let the user see indoor maps where available.
                map.setIndoorEnabled(true);

                // Enable my-location stuff
                map.setMyLocationEnabled(true);

                // Move the "camera" (view position) to our center point.
                map.moveCamera(CameraUpdateFactory.newLatLng(CENTER));
                // Then animate the markers while the map is drawing,
                // since you can't combine motion and zoom setting!
                final int zoom = 10;
                map.animateCamera(CameraUpdateFactory.zoomTo(zoom), 1500, null);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class getStations extends AsyncTask<URL, Integer, Long> {


        @Override
        protected Long doInBackground(URL... params) {
            String location = "http://www3.septa.org/hackathon/Arrivals/station_id_name.csv";
            URL website = null;
            try {
                website = new URL(location);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            URLConnection connection = null;
            try {
                connection = website.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inputLine;
            try {
                in.readLine();
                int i = 0;
                while ((inputLine = in.readLine()) != null) {

                    String[] line = inputLine.split(",");
                    stations.add(line[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long result) {

            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_list_item_1, stations);

            AutoCompleteTextView view1 = (AutoCompleteTextView)
                    findViewById(R.id.auto_complete_list1);
            view1.setAdapter(adapter1);

            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_list_item_1, stations);

            AutoCompleteTextView view2 = (AutoCompleteTextView)
                    findViewById(R.id.auto_complete_list2);
            view2.setAdapter(adapter2);
        }
    }
    public class getTrainList extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            URL website = null;
            try {
                website = new URL(params[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            URLConnection connection = null;
            try {
                connection = website.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            StringBuilder response = new StringBuilder();
            String inputLine;
            try {
                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String response)
        {
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(response);
            JsonArray array = root.getAsJsonArray();
            for(JsonElement x : array )
            {

                String train = x.getAsJsonObject().get("trainno").getAsString();// train number
                String s = x.getAsJsonObject().get("SOURCE").getAsString();// train source
                String d = x.getAsJsonObject().get("dest").getAsString();// train destination
                String lat = x.getAsJsonObject().get("lat").getAsString();// train latitute
                String lon = x.getAsJsonObject().get("lon").getAsString();// train longitute
                // source and dest are global
                if(s.equals(source) && d.equals(dest))
                {
                    trains.add(train);
                    String[] coordinate = {lat, lon};
                    trainLocations.put(train, coordinate);
                }
            }
            new getTimes().execute("http://www3.septa.org/hackathon/RRSchedules/");
        }
    }

    public class getTimes extends AsyncTask<String, Integer, HashMap<String, String>> {
        @Override
        protected HashMap<String, String> doInBackground(String... params) {
            HashMap<String, String> map = new HashMap<String, String>();
            for(String x : trains){
                URL website = null;
                try {
                    website = new URL(params[0]+x);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                URLConnection connection = null;
                try {
                    connection = website.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedReader in = null;
                try {
                    in = new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StringBuilder response = new StringBuilder();
                String inputLine;
                try {
                    while ((inputLine = in.readLine()) != null)
                        response.append(inputLine);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                map.put(x, response.toString());
            }
            return map;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> trainResponse)
        {
            Log.v("Tuan", "reach on post execute");
            ArrayList<HashMap<String, String>> trainInfo = new ArrayList<>();

            int trainCount = trainResponse.size();
            String[] from = {"train", "source", "dest", "dept", "arriv"};
            int[] to = {R.id.trainViewNo, R.id.trainViewSource, R.id.trainViewDest,
                    R.id.trainViewDept, R.id.trainViewArriv};
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("train", "TRAIN");
            map.put("source", "SOURCE");
            map.put("dest", "DESTINATION");
            map.put("dept", "DEPARTURE");
            map.put("arriv", "ARRIVAL");
            trainInfo.add(map);
            for(String x : trains)
            {
                map = new HashMap<String, String>();
                JsonParser jp = new JsonParser();
                JsonElement root = jp.parse(trainResponse.get(x));
                JsonArray array = root.getAsJsonArray();
                String dept = array.get(0).getAsJsonObject().get("est_tm").getAsString();
                Log.v("dept", dept);
                String arrival = null;
                for (JsonElement j : array) {

                    String temp = j.getAsJsonObject().get("station").getAsString();
                    String temparriv = j.getAsJsonObject().get("est_tm").getAsString();
                    if(temp.equals(dest))
                    {
                        arrival = temparriv;
                        Log.v("arriv", arrival);
                        break;
                    }
                }
                map.put("train", x);
                map.put("source", source);
                map.put("dest", dest);
                map.put("dept", dept);
                map.put("arriv", arrival);
                trainInfo.add(map);
            }
            SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, trainInfo,
                    R.layout.train_view_item, from, to);
            ListView list = (ListView)findViewById(R.id.trainListView);
            list.setAdapter(adapter);
        }
    }

    public class getTrainInfo extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            URL website = null;
            try {
                website = new URL(params[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            URLConnection connection = null;
            try {
                connection = website.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuilder response = new StringBuilder();
            String inputLine;
            try {
                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String response)
        {
            String station = null;
            String schedTime = null;
            String actTime = null;
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(response);
            JsonArray array = root.getAsJsonArray();
            for(int i = 0; i < array.size(); i++)
            {
                JsonElement x = array.get(i);
                JsonElement next = array.get(i + 1);
                String actTimeNext = next.getAsJsonObject().get("act_tm").getAsString();
                station = x.getAsJsonObject().get("station").getAsString();
                schedTime = x.getAsJsonObject().get("sched_tm").getAsString();
                actTime = x.getAsJsonObject().get("act_tm").getAsString();
                if(actTimeNext.equals("na"))
                {
                    Log.v("last station", station);
                    Log.v("scheduled time", schedTime);
                    Log.v("actual time", actTime);
                    break;
                }
            }
            Context context = getApplicationContext();
            CharSequence text = "Station: " +station +"\n"+ "Sched. Time: "+schedTime +"\n"
                    +"Act. Time: " +actTime;
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }
}
