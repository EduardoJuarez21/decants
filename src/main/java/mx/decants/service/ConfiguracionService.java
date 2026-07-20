package mx.decants.service;

import mx.decants.entity.Configuracion;
import mx.decants.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracionService {

    private final ConfiguracionRepository repo;

    public ConfiguracionService(ConfiguracionRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public String get(String clave, String defecto) {
        return repo.findById(clave).map(Configuracion::getValor).orElse(defecto);
    }

    @Transactional
    public void set(String clave, String valor) {
        Configuracion c = repo.findById(clave).orElse(new Configuracion(clave, valor));
        c.setValor(valor);
        repo.save(c);
    }

    @Transactional(readOnly = true)
    public String getStripeModo() {
        return get("stripe_modo", "live");
    }

    @Transactional
    public void setStripeModo(String modo) {
        set("stripe_modo", modo);
    }

    @Transactional(readOnly = true)
    public int getCostoEnvio() {
        try { return Integer.parseInt(get("envio_costo", "159")); }
        catch (NumberFormatException e) { return 159; }
    }

    @Transactional(readOnly = true)
    public int getUmbralEnvioGratis() {
        try { return Integer.parseInt(get("envio_umbral_gratis", "1299")); }
        catch (NumberFormatException e) { return 1299; }
    }

    @Transactional(readOnly = true)
    public String getTextoEnvioLocal() {
        return get("envio_texto_local", "Entregas personales en el Área Metropolitana de Monterrey");
    }

    @Transactional(readOnly = true)
    public String getWhatsappNegocio() {
        return get("whatsapp_negocio", "528119874977");
    }
}