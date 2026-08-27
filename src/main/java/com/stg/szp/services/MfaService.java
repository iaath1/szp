package com.stg.szp.services;

import org.springframework.stereotype.Service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.util.Utils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@AllArgsConstructor
public class MfaService {
    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public String getQrCodeImage(String secret, String email) throws Exception {
        QrData data = new QrData.Builder()
            .label(email)
            .secret(secret)
            .issuer("SZP-Project")
            .build();

        return Utils.getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
    }

    public boolean verifyCode(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }
}
