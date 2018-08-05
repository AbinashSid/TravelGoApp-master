package com.example.pallab.travelgoapp.Service;

import com.example.pallab.travelgoapp.Common.Common;
import com.example.pallab.travelgoapp.Model.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by PALLAB on 3/13/2018.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        updateTokenToServer(refreshedToken); // when have refresh toekn we need to update to our database
    }

    private void updateTokenToServer(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference  tokens = db.getReference(Common.token_tb1);
        Token token = new Token(refreshedToken);
        if (FirebaseAuth.getInstance().getCurrentUser() != null)//if already login must update token
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                     .setValue(token);

    }
}
