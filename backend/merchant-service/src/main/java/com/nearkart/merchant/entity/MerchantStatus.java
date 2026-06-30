package com.nearkart.merchant.entity;

/**
 * Lifecycle states for a merchant account.
 *
 * PENDING_KYC   -> merchant registered, no documents uploaded yet
 * KYC_SUBMITTED -> at least one KYC document uploaded, awaiting admin review
 * KYC_REJECTED  -> admin rejected one or more KYC documents
 * ACTIVE        -> KYC fully verified, merchant can operate shops
 * SUSPENDED     -> temporarily blocked by admin (policy violation, etc.)
 * DEACTIVATED   -> permanently closed / self-deleted
 */
public enum MerchantStatus {
    PENDING_KYC,
    KYC_SUBMITTED,
    KYC_REJECTED,
    ACTIVE,
    SUSPENDED,
    DEACTIVATED
}
