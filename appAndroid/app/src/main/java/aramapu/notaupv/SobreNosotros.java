package aramapu.notaupv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class SobreNosotros extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre_nosotros);

        setTitle(R.string.sobre_nosotros);

        TextView txtSobreNosotros = findViewById(R.id.textView_sobreNosotros);

        if (android.os.Build.VERSION.SDK_INT >= 26){
            txtSobreNosotros.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //onBackPressed();
                onBackPressed();
                return true;
        }
        return(super.onOptionsItemSelected(item));
    }

}
