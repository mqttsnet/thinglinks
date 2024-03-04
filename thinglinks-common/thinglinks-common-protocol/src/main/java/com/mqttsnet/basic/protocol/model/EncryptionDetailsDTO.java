package com.mqttsnet.basic.protocol.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * -----------------------------------------------------------------------------
 * File Name: EncryptionDetailsDto.java
 * -----------------------------------------------------------------------------
 * Description:
 * This class is used to encapsulate encryption and decryption details  , for message processing, providing necessary information without
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023-11-12 01:48
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
public class EncryptionDetailsDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * The key used for signing the data. This is usually a secret key
     * shared between the sender and the receiver to verify the integrity
     * of the message.
     */
    private String signKey;

    /**
     * The key used for encrypting the data. This is typically a symmetric key
     * used in encryption algorithms to ensure that the message contents are
     * not readable by unauthorized parties.
     */
    private String encryptKey;

    /**
     * The initialization vector (IV) used for encryption algorithms that require it.
     * The IV is a pseudo-random value and is used to ensure that the encryption
     * result is different for the same plaintext and key.
     */
    private String encryptVector;

    /**
     * Indicates the encryption method used. Different values represent different
     * encryption algorithms or no encryption.
     * For example, 0 might represent no encryption, 1 might represent AES, etc.
     */
    private Integer cipherFlag;
}
