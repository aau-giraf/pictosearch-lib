package dk.aau.cs.giraf.pictosearch;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * Used to display a message to the user. Message is included in constructor
 * @author Christian Klim Hansen, SW605f13 Parrot-group
 */
@SuppressLint("ValidFragment")
public class MessageDialogFragment extends DialogFragment {
	public String message;
		
	public MessageDialogFragment(String msg) {
		this.message = msg;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
			.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Do nothing
			}
		});
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
