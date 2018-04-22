package gr.academic.city.sdmd.servicesandreceivers;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ListResponse {

    @SerializedName("items")
    private List<Student> students = new ArrayList<>(0);

    public ListResponse() {
    }

    public List<Student> getStudents() {
        return students;
    }
}
