package com.example.aula31_32;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button carregar, salvar, camera, enviar, texto;
    EditText txsuperior, txinferior;
    TextView txt01, txt02;
    ImageView imagem;
    String arqImagem;
    File imagemFile;
    Uri imagemUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        carregar = findViewById(R.id.carregar);
        salvar = findViewById(R.id.salvar);
        camera = findViewById(R.id.camera);
        enviar = findViewById(R.id.enviar);
        texto = findViewById(R.id.texto);
        txsuperior = findViewById(R.id.txsuperior);
        txinferior = findViewById(R.id.txinferior);
        txt01 = findViewById(R.id.txt01);
        txt02 = findViewById(R.id.txt02);
        imagem = findViewById(R.id.imageView);

        salvar.setEnabled(false);
        enviar.setEnabled(false);

        texto.setOnClickListener(v -> {
            txt01.setText(txsuperior.getText().toString());
            txt02.setText(txinferior.getText().toString());
            txsuperior.setText("");
            txinferior.setText("");
        });

        carregar.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 2);
        });

        salvar.setOnClickListener(v -> {
            View relCenter = findViewById(R.id.relCenter);
            Bitmap bitmap = screenShot(relCenter);
            arqImagem = "Aula11_" + System.currentTimeMillis() + ".png";
            armazenar(bitmap, arqImagem);
            enviar.setEnabled(true);
        });

        camera.setOnClickListener(v -> abrirCameraAltaQualidade());

        enviar.setOnClickListener(v -> enviarTexto());

        if (!temCamera()) {
            camera.setEnabled(false);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new SensorFragment())
                .commit();
    }

    public Boolean temCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public static Bitmap screenShot(View v) {
        v.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public void armazenar(Bitmap bitmap, String arquivo) {
        String diretorio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Aula11";
        File dir = new File(diretorio);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(diretorio, arquivo);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Salvo com Sucesso!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro na hora de Salvar", Toast.LENGTH_SHORT).show();
        }
    }

    void enviarTexto() {
        String texto = txt01.getText().toString() + "\n" + txt02.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");

        intent.putExtra(Intent.EXTRA_TEXT, texto);

        ArrayList<Uri> uris = new ArrayList<>();
        if (imagemUri != null) {
            uris.add(imagemUri);
        }

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Aula11");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    Uri fileUri = FileProvider.getUriForFile(this, "com.example.aula31_32.fileprovider", file);
                    uris.add(fileUri);
                }
            }
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Compartilhar via..."));
    }

    private void abrirCameraAltaQualidade() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2);
        } else {
            try {
                File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CameraApp");
                if (!dir.exists()) dir.mkdirs();

                imagemFile = File.createTempFile("foto_", ".jpg", dir);
                imagemUri = FileProvider.getUriForFile(this, "com.example.aula31_32.fileprovider", imagemFile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imagemUri);
                startActivityForResult(intent, 3);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao abrir a câmera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri pegarImagem = data.getData();
            String[] diretorio = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(pegarImagem, diretorio, null, null, null);
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(diretorio[0]);
            String imagemDir = cursor.getString(index);
            cursor.close();
            imagem.setImageBitmap(BitmapFactory.decodeFile(imagemDir));
            salvar.setEnabled(true);
            enviar.setEnabled(false);
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagemFile.getAbsolutePath());
            if (bitmap != null) {
                imagem.setImageBitmap(bitmap);
                salvar.setEnabled(true);
                enviar.setEnabled(false);
            } else {
                Toast.makeText(this, "Erro ao carregar a imagem da câmera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Sem permissão de escrita suficiente!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCameraAltaQualidade();
            } else {
                Toast.makeText(this, "Permissão de câmera negada!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}