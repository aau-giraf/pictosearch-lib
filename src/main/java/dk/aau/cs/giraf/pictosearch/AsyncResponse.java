package dk.aau.cs.giraf.pictosearch;

import java.util.ArrayList;

import dk.aau.cs.giraf.models.core.Pictogram;

public interface AsyncResponse {
    void processFinish(ArrayList<Pictogram> output);
}
