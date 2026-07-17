package mx.decants.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

@Service
public class ImagenService {

    @Value("${app.uploads-dir:/app/uploads}")
    private String uploadsDir;

    public String guardarYConvertir(MultipartFile archivo, String categoria, String genero, String nombreArchivo)
            throws IOException, InterruptedException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen no puede estar vacío");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        String subdir = resolverSubdir(categoria, genero);
        Path dirSalida = Paths.get(uploadsDir, subdir);
        Files.createDirectories(dirSalida);

        String nombreWebp = nombreArchivo + ".webp";
        Path rutaSalida = dirSalida.resolve(nombreWebp);

        Path temp = Files.createTempFile("decants-", getExtension(archivo.getOriginalFilename()));
        try {
            archivo.transferTo(temp);

            ProcessBuilder pb = new ProcessBuilder(
                "cwebp", "-q", "82", temp.toString(), "-o", rutaSalida.toString()
            );
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            boolean termino = proc.waitFor(30, TimeUnit.SECONDS);

            if (!termino || proc.exitValue() != 0) {
                Files.copy(temp, rutaSalida, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }

        return "/" + subdir + "/" + nombreWebp;
    }

    private String resolverSubdir(String categoria, String genero) {
        if ("nicho-arabe".equals(categoria)) return "img/arabe";
        if ("mujer".equals(genero))           return "img/alta-perfumeria/mujer";
        return "img/alta-perfumeria/hombre";
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }
}
