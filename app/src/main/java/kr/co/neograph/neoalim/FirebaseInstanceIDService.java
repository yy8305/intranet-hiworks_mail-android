package kr.co.neograph.neoalim;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIdService";
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG,token);

        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token){

    }
}
