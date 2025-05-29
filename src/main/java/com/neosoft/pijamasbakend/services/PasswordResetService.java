package com.neosoft.pijamasbakend.services;

import com.neosoft.pijamasbakend.entities.PasswordResetToken;
import com.neosoft.pijamasbakend.models.RestablecerPassDto;
import com.neosoft.pijamasbakend.models.SolicitarResetDto;
import com.neosoft.pijamasbakend.models.VerificarCodigoDto;
import com.neosoft.pijamasbakend.repositories.AdministrativoRepository;
import com.neosoft.pijamasbakend.repositories.ClienteRepository;
import com.neosoft.pijamasbakend.repositories.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepo;

    @Autowired
    private ClienteRepository clienteRepo;

    @Autowired
    private AdministrativoRepository adminRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.reset.expire-minutes}")
    private int expireMinutes;

    @Transactional
    public void solicitarCodigo(SolicitarResetDto dto) {

        String email = dto.getEmail().trim().toLowerCase();

        boolean existe = clienteRepo.existsByEmail(email) || adminRepo.existsByEmail(email);
        if (!existe) {
            throw new ResponseStatusException(NOT_FOUND, "El correo no pertenece a ningún usuario");
        }

        tokenRepo.deleteByEmail(email);

        String code = "%06d".formatted(ThreadLocalRandom.current().nextInt(1_000_000));
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setCode(code);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(expireMinutes));
        tokenRepo.save(token);

        enviarCorreo(email, code);
    }

    @Transactional(readOnly = true)
    public void verificarCodigo(VerificarCodigoDto dto) {
        PasswordResetToken t = tokenRepo
                .findByEmailAndCodeAndUsedFalse(dto.getEmail(), dto.getCodigo())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Código inválido o ya usado"));
        if (t.isExpired()) {
            throw new ResponseStatusException(BAD_REQUEST, "Código expirado");
        }
    }

    @Transactional
    public void restablecer(RestablecerPassDto dto) {
        PasswordResetToken token = tokenRepo
                .findByEmailAndCodeAndUsedFalse(dto.getEmail(), dto.getCodigo())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Código inválido o ya usado"));

        if (token.isExpired()) {
            throw new ResponseStatusException(BAD_REQUEST, "Código expirado");
        }

        boolean actualizado = clienteRepo.findByEmail(dto.getEmail())
                .map(c -> {
                    c.setHashedPassword(encoder.encode(dto.getNewPassword()));
                    clienteRepo.save(c);
                    return true;
                })
                .orElseGet(() -> adminRepo.findByEmail(dto.getEmail())
                        .map(a -> {
                            a.setHashedPassword(encoder.encode(dto.getNewPassword()));
                            a.setLastUpdate(LocalDateTime.now());
                            adminRepo.save(a);
                            return true;
                        })
                        .orElse(false));

        if (!actualizado) {
            throw new ResponseStatusException(NOT_FOUND, "Usuario no encontrado al cambiar contraseña");
        }

        token.setUsed(true);
    }

    private void enviarCorreo(String to, String code) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Código de recuperación de contraseña");
        msg.setText("""
                Has solicitado restablecer tu contraseña.
                Tu código es: %s
                Este código vence en %d minutos.
                
                Si no lo solicitaste, ignora este mensaje.
                """.formatted(code, expireMinutes));

        mailSender.send(msg);
    }

}
