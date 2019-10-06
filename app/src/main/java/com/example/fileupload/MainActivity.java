package com.example.fileupload;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    ImageButton i1, i2;
    TextView notification,notification1;
    Uri pdfuri;//stores the actual path to your local file


    FirebaseStorage storage;//used for uploading files ; for example: pdf
    FirebaseDatabase database;//used for storing urls of uploaded files
    ProgressDialog dialog;//progress dialog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        i1 = (ImageButton) findViewById(R.id.image1);
        i2 = (ImageButton) findViewById(R.id.image2);
        notification = (TextView) findViewById(R.id.text1);
        notification1 = (TextView) findViewById(R.id.fileupload);
        storage = FirebaseStorage.getInstance();//Return an object of firebase storage
        database = FirebaseDatabase.getInstance();//Return an object of firebase database

    }

    public void load(View v) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectpdf();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9); //ask for permission
        }
    }
   //after asking permission this  method will acknowledge
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==9&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            selectpdf();
        }else
        {
            Toast.makeText(MainActivity.this,"PLEASE PROVIDE PERMISSION",Toast.LENGTH_SHORT).show();
        }
    }

    private void selectpdf()
    {
        //to offer user to select the file from file manager

        //use Intent
        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);//to fetch the file
        startActivityForResult(intent,86);

    }
     //use for acknowledgement for the selectpdf
    //automatically get invoked after startactivityforresult
    //open file manager or exit it successfully at the end
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==86&&resultCode==RESULT_OK&&data!=null)
        {
            pdfuri=data.getData();//returns the uri of selected file
            notification1.setText("A file is selected "+data.getData().getLastPathSegment());
        }else
        {
            Toast.makeText(MainActivity.this,"PLEASE SELECT FILE",Toast.LENGTH_SHORT).show();
        }
    }

    public void upload(View v)
    {
          if(pdfuri!=null)//user has successfully uploaded the file
              uploadfile(pdfuri);
          else
              Toast.makeText(MainActivity.this,"SELECT A FILE",Toast.LENGTH_SHORT).show();
    }
    private void uploadfile(Uri pdfuri)
    {
        dialog=new ProgressDialog(this);
        dialog.setMessage("Uploading File...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.show();
        final String name=System.currentTimeMillis()+"";
        final StorageReference storageReference=storage.getReference();//return the root path
        storageReference.child("uploads").child(name).putFile(pdfuri)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String url=storageReference.getDownloadUrl().toString();  //return the url of your uploaded file
                //store the url in the realtime database
                DatabaseReference databaseReference=database.getReference();//return the path to root
                databaseReference.child(name).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this,"FILE SUCCESSFULLY UPLOADED",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this,"FILE NOT SUCCESSFULLY UPLOADED",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"FILE NOT SUCCESSFULLY UPLOADED",Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //track the progress of the file upload

                 int currentprogress=(int)(100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                 dialog.setProgress(currentprogress);


            }
        });



    }
}
