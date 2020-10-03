package com.example.tanggapdarurat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static java.util.Locale.ENGLISH;


public class Kamera extends AppCompatActivity {
    Button getlocation,opengmaps, buttonambulance, buttonsar, buttonpolisi, buttonpemadam;
    TextView lat, lon;
    ImageView picture;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private ProgressDialog progressDialog;
    StorageReference mStorage;
    private static final int CAMERA_REQUEST_CODE = 1;
    Uri picUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kamera);
        lat = findViewById(R.id.latitude);
        lon = findViewById(R.id.longitude);
        getlocation = findViewById(R.id.button);
        opengmaps = findViewById(R.id.seemaps);
        picture = (ImageView) findViewById(R.id.gambar);
        buttonambulance = (Button) findViewById(R.id.ambulance);
        buttonpemadam = (Button) findViewById(R.id.Pemadam);
        buttonpolisi = (Button) findViewById(R.id.polisi);
        buttonsar = (Button) findViewById(R.id.SAR);

        mStorage = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);


        //opengmaps adalah button untuk membuka gmaps aplikasi di lokasi user berada
        opengmaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("geo:"+ lat.toString() + "," + lon.toString() + "q=" + lat.toString() + "," + lon.toString() ); //a1 itu latitude dan a2 itu longitude
                //Intent uri = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:<lat>,<long>?q=<lat>,<long>(Label+Name)"));
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(String.valueOf(uri)));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });

        //getlocation adalah button untuk mendapatkan lokasi berupa latitude dan longitude pada user, dan akan dikirimkan ke firebase
        getlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Kamera.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    loc();
                    //  dispatchTakePictureIntent();

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,CAMERA_REQUEST_CODE);
                   /* Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    File file=getOutputMediaFile(1);
                    picUri = Uri.fromFile(file); // create
                    i.putExtra(MediaStore.EXTRA_OUTPUT,picUri); // set the image file

                    startActivityForResult(i, CAMERA_REQUEST_CODE);

                    */

                }
            }
        });

        buttonambulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getdataambulance();
            }
        });

        buttonsar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getdatasar();
            }
        });

        buttonpolisi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getdatapolisi();
            }
        });

        buttonpemadam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getdatapemadam();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK ) {

            //get the camera image
            //Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            //byte[] databaos = baos.toByteArray();

            //set the image into imageview
            picture.setImageBitmap(bitmap);
            //String img = "fire"

            //Firebase storage folder where you want to put the images
           // StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            //name of the image file (add time to have different files to avoid rewrite on the same file)
            //StorageReference imagesRef = storageRef.child("" + new Date().getTime());
            //send this name to database
            //upload image
            /*UploadTask uploadTask = imagesRef.putBytes(databaos);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(Kamera.this, "Sending failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                }
            } );*/}
    }


    //untuk mendapakan lokasi kita harus malekukan request permission pada user jika tidak maka akan terjadi error
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loc();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //pada class ini berfungsi untuk mendapatkan lokasi melalui data gps pada handphone user
    private void loc() {
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(300);
        request.setPriority(request.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(Kamera.this).
                requestLocationUpdates(request, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(Kamera.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            double latitude = locationResult.getLastLocation().getLatitude();
                            double longitude = locationResult.getLastLocation().getLongitude();
                            lat.setText(String.format(String.valueOf(latitude)));
                            lon.setText(String.format(String.valueOf(longitude)));
                            //firebase(latitude,longitude);
                        }
                    }
                }, Looper.getMainLooper());
    }


    private void ngirimgambarambulance(){

        progressDialog.setMessage("Uploding image...");
        progressDialog.show();
        BitmapDrawable drawable = (BitmapDrawable) picture.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] databaos = baos.toByteArray();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        //name of the image file (add time to have different files to avoid rewrite on the same file)
        StorageReference imagesRef = storageRef.child("Ambulance" + new Date().getTime());
        //send this name to database
        //upload image
            UploadTask uploadTask = imagesRef.putBytes(databaos);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(Kamera.this, "Sending failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                }
            } );

    }

    private void ngirimgambarpolisi(){

        progressDialog.setMessage("Uploding image...");
        progressDialog.show();
        BitmapDrawable drawable = (BitmapDrawable) picture.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] databaos = baos.toByteArray();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        //name of the image file (add time to have different files to avoid rewrite on the same file)
        StorageReference imagesRef = storageRef.child("Polisi" + new Date().getTime());
        //send this name to database
        //upload image
        UploadTask uploadTask = imagesRef.putBytes(databaos);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(Kamera.this, "Sending failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
            }
        } );

    }

    private void ngirimgambarsar(){

        progressDialog.setMessage("Uploding image...");
        progressDialog.show();
        BitmapDrawable drawable = (BitmapDrawable) picture.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] databaos = baos.toByteArray();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        //name of the image file (add time to have different files to avoid rewrite on the same file)
        StorageReference imagesRef = storageRef.child("SAR" + new Date().getTime());
        //send this name to database
        //upload image
        UploadTask uploadTask = imagesRef.putBytes(databaos);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(Kamera.this, "Sending failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
            }
        } );

    }

    private void ngirimgambarpemadam(){

        progressDialog.setMessage("Uploding image...");
        progressDialog.show();
        BitmapDrawable drawable = (BitmapDrawable) picture.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] databaos = baos.toByteArray();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        //name of the image file (add time to have different files to avoid rewrite on the same file)
        StorageReference imagesRef = storageRef.child("Pemadam" + new Date().getTime());
        //send this name to database
        //upload image
        UploadTask uploadTask = imagesRef.putBytes(databaos);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(Kamera.this, "Sending failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
            }
        } );

    }

    //pada class ini bertujuan untuk mengirim data lokasi user ke database yg disiapkan
    private void firebaseambulance(final double latitude, final double longitude){
        final String decodedlat = EncodeString(String.valueOf(latitude));
        final String decodedlon = EncodeString(String.valueOf(longitude));
        //Database reference
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.child("latitude").child(decodedlat).exists())){
                    HashMap<String, Object> userDataMap = new HashMap<>();
                    userDataMap.put("longitude", decodedlon);
                    userDataMap.put("latitude", decodedlat);


                    RootRef.child("Ambulance").child(decodedlat).updateChildren(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Kamera.this, "Get Loc", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(Kamera.this, "Failed to get loc", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void firebasepolisi(final double latitude, final double longitude){
        final String decodedlat = EncodeString(String.valueOf(latitude));
        final String decodedlon = EncodeString(String.valueOf(longitude));
        //Database reference
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.child("latitude").child(decodedlat).exists())){
                    HashMap<String, Object> userDataMap = new HashMap<>();
                    userDataMap.put("longitude", decodedlon);
                    userDataMap.put("latitude", decodedlat);


                    RootRef.child("Polisi").child(decodedlat).updateChildren(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Kamera.this, "Get Loc", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(Kamera.this, "Failed to get loc", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void firebasesar(final double latitude, final double longitude){
        final String decodedlat = EncodeString(String.valueOf(latitude));
        final String decodedlon = EncodeString(String.valueOf(longitude));
        //Database reference
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.child("latitude").child(decodedlat).exists())){
                    HashMap<String, Object> userDataMap = new HashMap<>();
                    userDataMap.put("longitude", decodedlon);
                    userDataMap.put("latitude", decodedlat);


                    RootRef.child("SAR").child(decodedlat).updateChildren(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Kamera.this, "Get Loc", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(Kamera.this, "Failed to get loc", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void firebasepemadam(final double latitude, final double longitude){
        final String decodedlat = EncodeString(String.valueOf(latitude));
        final String decodedlon = EncodeString(String.valueOf(longitude));
        //Database reference
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.child("latitude").child(decodedlat).exists())){
                    HashMap<String, Object> userDataMap = new HashMap<>();
                    userDataMap.put("longitude", decodedlon);
                    userDataMap.put("latitude", decodedlat);


                    RootRef.child("Pemadam").child(decodedlat).updateChildren(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Kamera.this, "Get Loc", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(Kamera.this, "Failed to get loc", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getdataambulance(){

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Ambulance");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot1 : snapshot.getChildren()){

                    String value1 = DecodeString(String.valueOf(dataSnapshot1.child("latitude").getValue()));
                    String value2 = DecodeString(String.valueOf(dataSnapshot1.child("longitude").getValue()));
                    double lat2 = Double.parseDouble(value1);
                    double lon2 = Double.parseDouble(value2);
                    double lat1 = Double.parseDouble(lat.getText().toString());
                    double lon1 = Double.parseDouble(lon.getText().toString());
                    Location startPoint=new Location("locationA");
                    startPoint.setLatitude(lat1);
                    startPoint.setLongitude(lon1);

                    Location endPoint=new Location("locationA");
                    endPoint.setLatitude(lat2);
                    endPoint.setLongitude(lon2);
                    double distance=startPoint.distanceTo(endPoint);
                    Log.i("Distance", String.valueOf(distance)+lat1+lon1+lat2+lon2);

                    if(distance < 500){
                        Toast.makeText(Kamera.this, "Sudah ada lampiran masuk", Toast.LENGTH_SHORT).show();
                        //if(distance==0.0){

                        //startActivity(new Intent(getApplicationContext(),Login.class));
                        break;
                    } else {
                        double latitude = Double.parseDouble(lat.getText().toString());
                        double longitude = Double.parseDouble(lon.getText().toString());
                        //String latitude = lat.getText().toString();
                        //String longitude = lon.getText().toString();
                        firebaseambulance(latitude,longitude);
                        ngirimgambarambulance();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getdatapolisi(){

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Polisi");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot1 : snapshot.getChildren()){

                    String value1 = DecodeString(String.valueOf(dataSnapshot1.child("latitude").getValue()));
                    String value2 = DecodeString(String.valueOf(dataSnapshot1.child("longitude").getValue()));
                    double lat2 = Double.parseDouble(value1);
                    double lon2 = Double.parseDouble(value2);
                    double lat1 = Double.parseDouble(lat.getText().toString());
                    double lon1 = Double.parseDouble(lon.getText().toString());
                    Location startPoint=new Location("locationA");
                    startPoint.setLatitude(lat1);
                    startPoint.setLongitude(lon1);

                    Location endPoint=new Location("locationA");
                    endPoint.setLatitude(lat2);
                    endPoint.setLongitude(lon2);
                    double distance=startPoint.distanceTo(endPoint);
                    Log.i("Distance", String.valueOf(distance)+lat1+lon1+lat2+lon2);

                    if(distance < 500){
                        Toast.makeText(Kamera.this, "Sudah ada lampiran masuk", Toast.LENGTH_SHORT).show();
                        //if(distance==0.0){

                        //startActivity(new Intent(getApplicationContext(),Login.class));
                        break;
                    } else {
                        double latitude = Double.parseDouble(lat.getText().toString());
                        double longitude = Double.parseDouble(lon.getText().toString());
                        //String latitude = lat.getText().toString();
                        //String longitude = lon.getText().toString();
                        firebasepolisi(latitude,longitude);
                        ngirimgambarpolisi();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getdatapemadam(){

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Pemadam");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot1 : snapshot.getChildren()){

                    String value1 = DecodeString(String.valueOf(dataSnapshot1.child("latitude").getValue()));
                    String value2 = DecodeString(String.valueOf(dataSnapshot1.child("longitude").getValue()));
                    double lat2 = Double.parseDouble(value1);
                    double lon2 = Double.parseDouble(value2);
                    double lat1 = Double.parseDouble(lat.getText().toString());
                    double lon1 = Double.parseDouble(lon.getText().toString());
                    Location startPoint=new Location("locationA");
                    startPoint.setLatitude(lat1);
                    startPoint.setLongitude(lon1);

                    Location endPoint=new Location("locationA");
                    endPoint.setLatitude(lat2);
                    endPoint.setLongitude(lon2);
                    double distance=startPoint.distanceTo(endPoint);
                    Log.i("Distance", String.valueOf(distance)+lat1+lon1+lat2+lon2);

                    if(distance < 500){
                        Toast.makeText(Kamera.this, "Sudah ada lampiran masuk", Toast.LENGTH_SHORT).show();
                        //if(distance==0.0){

                        //startActivity(new Intent(getApplicationContext(),Login.class));
                        break;
                    } else {
                        double latitude = Double.parseDouble(lat.getText().toString());
                        double longitude = Double.parseDouble(lon.getText().toString());
                        //String latitude = lat.getText().toString();
                        //String longitude = lon.getText().toString();
                        firebasepemadam(latitude,longitude);
                        ngirimgambarpemadam();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getdatasar(){

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("SAR");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot1 : snapshot.getChildren()){

                    String value1 = DecodeString(String.valueOf(dataSnapshot1.child("latitude").getValue()));
                    String value2 = DecodeString(String.valueOf(dataSnapshot1.child("longitude").getValue()));
                    double lat2 = Double.parseDouble(value1);
                    double lon2 = Double.parseDouble(value2);
                    double lat1 = Double.parseDouble(lat.getText().toString());
                    double lon1 = Double.parseDouble(lon.getText().toString());
                    Location startPoint=new Location("locationA");
                    startPoint.setLatitude(lat1);
                    startPoint.setLongitude(lon1);

                    Location endPoint=new Location("locationA");
                    endPoint.setLatitude(lat2);
                    endPoint.setLongitude(lon2);
                    double distance=startPoint.distanceTo(endPoint);
                    Log.i("Distance", String.valueOf(distance)+lat1+lon1+lat2+lon2);

                    if(distance < 500){
                        Toast.makeText(Kamera.this, "Sudah ada lampiran masuk", Toast.LENGTH_SHORT).show();
                        //if(distance==0.0){

                        //startActivity(new Intent(getApplicationContext(),Login.class));
                        break;
                    } else {
                        double latitude = Double.parseDouble(lat.getText().toString());
                        double longitude = Double.parseDouble(lon.getText().toString());
                        //String latitude = lat.getText().toString();
                        //String longitude = lon.getText().toString();
                        firebasesar(latitude,longitude);
                        ngirimgambarsar();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    //database untuk firebase tidak bisa menerima "." atau "/" , dan masih banyak lagi. melainkan firebase hanya bisa menerima ","
    public static String EncodeString(String string) {
        return string.replace(".", ",");
    }
    public static String DecodeString(String string) {
        return string.replace(",", ".");
    }

}