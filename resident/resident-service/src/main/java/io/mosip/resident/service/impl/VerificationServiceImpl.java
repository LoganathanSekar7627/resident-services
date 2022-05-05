package io.mosip.resident.service.impl;

import static io.mosip.resident.service.impl.ResidentOtpServiceImpl.EMAIL_CHANNEL;
import static io.mosip.resident.service.impl.ResidentOtpServiceImpl.PHONE_CHANNEL;

import java.security.NoSuchAlgorithmException;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.dto.VerificationStatusDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.VerificationService;
import io.mosip.resident.util.AuditUtil;

@Component
public class VerificationServiceImpl implements VerificationService {

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private IdentityServiceImpl identityServiceImpl;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataServiceImpl.class);

    @Override
    public VerificationResponseDTO checkChannelVerificationStatus(String channel, String individualId) throws ResidentServiceCheckedException, NoSuchAlgorithmException {
        logger.debug("VerificationServiceImpl::checkChannelVerificationStatus::Start");
        VerificationResponseDTO verificationResponseDTO = new VerificationResponseDTO();
        boolean verificationStatus = false;
        IdentityDTO identityDTO = identityServiceImpl.getIdentity(individualId);

        String uin ="";
        String email ="";
        String phone ="";

        if (identityDTO != null) {
            uin = identityDTO.getUIN();
            email = identityDTO.getEmail();
            phone = identityDTO.getPhone();
        }
        String idaToken = identityServiceImpl.getIdaToken(uin);
        String id;
        if(email != null && channel.equalsIgnoreCase(EMAIL_CHANNEL) ) {
			id= email+idaToken;
		} else if(phone != null && channel.equalsIgnoreCase(PHONE_CHANNEL) ) {
			id= phone+idaToken;
		} else {
			throw new ResidentServiceException(ResidentErrorCode.CHANNEL_IS_NOT_VALID.getErrorCode(),
					ResidentErrorCode.CHANNEL_IS_NOT_VALID.getErrorMessage());
		}
        byte[] idBytes = id.getBytes();
        String hash = HMACUtils2.digestAsPlainText(idBytes);
        ResidentTransactionEntity residentTransactionEntity = residentTransactionRepository.findByAid(hash);
        if (residentTransactionEntity != null) {
            if(residentTransactionEntity.getStatusCode().equalsIgnoreCase("OTP_VERIFIED")){
                verificationStatus = true;
            }
        }
        VerificationStatusDTO verificationStatusDTO = new VerificationStatusDTO();
        verificationStatusDTO.setVerificationStatus(verificationStatus);
        verificationResponseDTO.setResponse(verificationStatusDTO);
        verificationResponseDTO.setId("mosip.resident.channel.verification.status");
        verificationResponseDTO.setVersion("v1");
        verificationResponseDTO.setResponseTime(DateTime.now().toString());

        return verificationResponseDTO;
    }
}

