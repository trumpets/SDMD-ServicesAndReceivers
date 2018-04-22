package gr.academic.city.sdmd.servicesandreceivers;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by trumpets on 3/14/16.
 */
public class StudentService extends IntentService {

    private static final String LOG_TAG = "StudentService";

    public static final String ACTION_CREATE_STUDENT = "gr.academic.city.sdmd.servicesandreceivers.CREATE_STUDENT";
    public static final String ACTION_GET_STUDENTS = "gr.academic.city.sdmd.servicesandreceivers.GET_STUDENTS";

    public static final String ACTION_CREATE_STUDENT_RESULT = "gr.academic.city.sdmd.servicesandreceivers.CREATE_STUDENT_RESULT";
    public static final String ACTION_GET_STUDENTS_RESULT = "gr.academic.city.sdmd.servicesandreceivers.GET_STUDENTS_RESULT";

    public static final String EXTRA_FIRST_NAME = "first.name";
    public static final String EXTRA_LAST_NAME = "last.name";
    public static final String EXTRA_AGE = "age";

    public static final String EXTRA_CREATE_STUDENT_RESULT = "create.student.result";
    public static final String EXTRA_STUDENTS_RESULT = "students.result";

    private static final String GET_STUDENTS_URL = "https://city-201617.appspot.com/_ah/api/students/v1/student";
    private static final String CREATE_STUDENTS_URL = "https://city-201617.appspot.com/_ah/api/students/v1/student";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public StudentService() {
        super("Student Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (ACTION_CREATE_STUDENT.equals(action)) {
            createStudent(intent);
        } else if (ACTION_GET_STUDENTS.equals(action)) {
            getStudents(intent);
        } else {
            throw new UnsupportedOperationException("No implementation for action " + action);
        }
    }

    private void createStudent(Intent intent) {

        try {
            URL url = new URL(CREATE_STUDENTS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.addRequestProperty("Content-Type", "application/json");

            String firstName = intent.getStringExtra(EXTRA_FIRST_NAME);
            String lastName = intent.getStringExtra(EXTRA_LAST_NAME);
            String age = intent.getStringExtra(EXTRA_AGE);

            Student student = new Student();
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setAge(age);

            String studentJson = new Gson().toJson(student);

            Log.d(LOG_TAG, studentJson);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            writer.write(studentJson);
            writer.flush();
            writer.close();

            conn.getOutputStream().close();

            // Starts the post
            conn.connect();

            int response = conn.getResponseCode();

            Log.d(LOG_TAG, "The response is: " + response);

            Intent resultIntent = new Intent(ACTION_CREATE_STUDENT_RESULT);
            resultIntent.putExtra(EXTRA_CREATE_STUDENT_RESULT, "Created student. Server responded with status " + response);

            LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception creating students", e);
        }
    }

    private void getStudents(Intent intent) {
        InputStream is = null;

        try {
            URL url = new URL(GET_STUDENTS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();

            int response = conn.getResponseCode();
            Log.d(LOG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a bitmap
            String result = convertStreamToString(is);

            Intent resultIntent = new Intent(ACTION_GET_STUDENTS_RESULT);
            resultIntent.putExtra(EXTRA_STUDENTS_RESULT, result);

            LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception fetching students", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Exception closing stream", e);
                }
            }
        }
    }

    private String convertStreamToString(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        return baos.toString();
    }
}
