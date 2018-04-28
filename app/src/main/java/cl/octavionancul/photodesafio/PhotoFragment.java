package cl.octavionancul.photodesafio;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.frosquivel.magicalcamera.MagicalCamera;
import com.frosquivel.magicalcamera.MagicalPermissions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class PhotoFragment extends Fragment {

    private MagicalPermissions magicalPermissions;
    //The pixel percentage is declare like an percentage of 100, if your value is 50, the photo will have the middle quality of your camera.
// this value could be only 1 to 100.
    private int RESIZE_PHOTO_PIXELS_PERCENTAGE = 40;
    private MagicalCamera magicalCamera;
    private ImageView imageView;

    public PhotoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = view.findViewById(R.id.photoIv);

        String[] permissions = new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        magicalPermissions = new MagicalPermissions(this, permissions);
        magicalCamera = new MagicalCamera(getActivity(),RESIZE_PHOTO_PIXELS_PERCENTAGE, magicalPermissions);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Map<String, Boolean> map = magicalPermissions.permissionResult(requestCode, permissions, grantResults);
        for (String permission : map.keySet()) {
            Log.d("PERMISSIONS", permission + " was: " + map.get(permission));
        }
        //Following the example you could also
        //locationPermissions(requestCode, permissions, grantResults);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //CALL THIS METHOD EVER
        magicalCamera.resultPhoto(requestCode, resultCode, data);

        if(RESULT_OK ==resultCode){

            Bitmap photo = magicalCamera.getPhoto();

        //this is for rotate picture in this method
        //magicalCamera.resultPhoto(requestCode, resultCode, data, MagicalCamera.ORIENTATION_ROTATE_180);

        //with this form you obtain the bitmap (in this example set this bitmap in image view)
     //  imageView.setImageBitmap(photo);



        //if you need save your bitmap in device use this method and return the path if you need this
        //You need to send, the bitmap picture, the photo name, the directory name, the picture type, and autoincrement photo name if           //you need this send true, else you have the posibility or realize your standard name for your pictures.
        String path = magicalCamera.savePhotoInMemoryDevice(photo,"myPhotoName","myDirectoryName", MagicalCamera.JPEG, true);

            if(path != null){
                imageView.setImageBitmap(photo);
                String photoName = path.split("/myDirectoryName/")[1];
                Log.d("path",photoName);

                uploadPhoto(path,photoName);
                Log.d("path",path);
                Toast.makeText(getContext(), "The photo is save in device, please check this path: " + path, Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getContext(), "Sorry your photo dont write in device", Toast.LENGTH_SHORT).show();
        }

    }

    public void takePhoto(){

        magicalCamera.takeFragmentPhoto(PhotoFragment.this);
    }

    public void uploadPhoto(String path, String photoName) {

        path = "file://" + path;
        String url = "gs://photodesafio-33f34.appspot.com/folderExample/"+photoName;

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(url);

        storageReference.putFile(Uri.parse(path)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String url = taskSnapshot.getDownloadUrl().toString();
                  Log.d("url",url);

                  url= url.split("&token")[0];

                Log.d("url",url);
                Toast.makeText(getContext(), "Foto subida", Toast.LENGTH_SHORT).show();


            }
        });

    }
}
