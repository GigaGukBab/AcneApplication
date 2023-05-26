package com.example.acneapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyPageActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private TextView userNameTextView;
    private ImageView userProfileImageView;
    private Spinner skinTypeSpinner;
    private Spinner acneTypeSpinner;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        userNameTextView = findViewById(R.id.user_name_textview);
        userProfileImageView = findViewById(R.id.user_profile_image);
        skinTypeSpinner = findViewById(R.id.skin_type_spinner);
        acneTypeSpinner = findViewById(R.id.acne_type_spinner);
        saveButton = findViewById(R.id.save_button);

        displayUserNameAndProfileImage();
        setUpSpinners();
        loadSkinAndAcneData();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSkinAndAcneData();
            }
        });
    }

    private void displayUserNameAndProfileImage() {
        if (currentUser != null) {
            userNameTextView.setText(currentUser.getDisplayName());

            if (currentUser.getPhotoUrl() != null) {
                // Load user profile image using Glide
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop() // Optional: if you want the image to be displayed as a circle
                        .into(userProfileImageView);
            }
        }
    }


    private void setUpSpinners() {
        ArrayAdapter<CharSequence> skinTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.skin_types, android.R.layout.simple_spinner_item);
        skinTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skinTypeSpinner.setAdapter(skinTypeAdapter);

        ArrayAdapter<CharSequence> acneTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.acne_types, android.R.layout.simple_spinner_item);
        acneTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        acneTypeSpinner.setAdapter(acneTypeAdapter);
    }

    private void loadSkinAndAcneData() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);
            userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String savedSkinType = documentSnapshot.getString("skinType");
                        String savedAcneType = documentSnapshot.getString("acneType");

                        setSpinnerSelection(skinTypeSpinner, savedSkinType, R.array.skin_types);
                        setSpinnerSelection(acneTypeSpinner, savedAcneType, R.array.acne_types);
                    }
                }
            });
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value, int arrayResourceId) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        String[] stringArray = getResources().getStringArray(arrayResourceId);
        int position = Arrays.asList(stringArray).indexOf(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }

    private void saveSkinAndAcneData() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);

            String selectedSkinType = skinTypeSpinner.getSelectedItem().toString();
            String selectedAcneType = acneTypeSpinner.getSelectedItem().toString();

            Map<String, Object> userData = new HashMap<>();
            userData.put("skinType", selectedSkinType);
            userData.put("acneType", selectedAcneType);

            userDocRef.set(userData, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(MyPageActivity.this, "피부 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MyPageActivity.this, "피부 정보 저장에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}