package Push_notifi;


import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class Push_noti_service extends  FirebaseMessagingService{

    @Override // 토큰 생성 시 실행 되는
    public void onNewToken(@NonNull String token) {
        Log.d("new token","New token"+token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        if(message.getNotification() != null) {
            //메시지 정보 추출
            String msgTitle = message.getNotification().getTitle();
            String msgBody = message.getNotification().getBody();

            //서버 로그 작성
            Log.d("FCM new","알림 메시지 : "+ msgTitle);

            //앱 알림 클릭시 이동할 클래스 설정
            Intent intent = new Intent(this, Push_noti_Main.class);
            // 스택 맨 위로 위치/ 나머지 스택 제거
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
        }
    }
}
