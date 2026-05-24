package com.alexbui.nutritrack.data

/**
 * PhoneUtils provides phone number normalization for NutriTrack
 *
 * Handles phone numbers in various input formats. Australian numbers
 * (stored with 61 prefix) additionally accept local 0x format,
 * UNLESS the same trailing digits exist under a different country code
 * in the database — in that case full form is required to avoid ambiguity.
 *
 * No cross-region matching — numbers with different country codes
 * but identical remaining digits will never match each other.
 *
 * Supported input formats given DB stores 61436567330 (Australian)
 * and no other country has 436567330:
 * - +61436567330 (international with +)
 * - 61436567330  (full without +)
 * - 0436567330   (local Australian format)
 *
 * If DB also stores 84436567330 (same trailing digits, different country):
 * - +61436567330 ✅
 * - 61436567330  ✅
 * - 0436567330   ❌ rejected — ambiguous, full form required
 */
object PhoneUtils {

    /**
     * Checks if a local 0x format input is ambiguous across the full
     * list of stored phone numbers
     *
     * Ambiguous means: converting 0x to 61x would produce trailing digits
     * that also exist under a different country code in the database
     *
     * @param inputDigits raw digits from user input (already filtered)
     * @param allStoredNumbers full list of phone numbers from Room
     * @return true if the 0x input is ambiguous and should be rejected
     */
    private fun isAmbiguousLocal(inputDigits: String, allStoredNumbers: List<String>): Boolean {
        val converted = "61" + inputDigits.drop(1)
        val trailing = inputDigits.drop(1) // digits after the leading 0

        // Check if any stored number has the same trailing digits but different country code
        return allStoredNumbers
            .map { it.filter { c -> c.isDigit() } }
            .any { stored ->
                stored != converted && stored.endsWith(trailing)
            }
    }

    /**
     * Normalizes a phone number for comparison against a stored value
     *
     * If stored number is Australian (starts with 61), also accepts
     * local 0x format — but only if no other country code shares
     * the same trailing digits in the database.
     *
     * @param input raw phone number string from user input
     * @param stored phone number stored in Room for this user
     * @param allStoredNumbers full list of all phone numbers in Room
     * @return normalized input digits ready for comparison against stored
     */
    fun normalisePhone(input: String, stored: String, allStoredNumbers: List<String>): String {
        val inputDigits = input.filter { it.isDigit() }
        val storedDigits = stored.filter { it.isDigit() }

        if (storedDigits.startsWith("61") &&
            inputDigits.startsWith("0") &&
            "61" + inputDigits.drop(1) == storedDigits &&
            !isAmbiguousLocal(inputDigits, allStoredNumbers)) {
            return "61" + inputDigits.drop(1)
        }

        return inputDigits
    }

    /**
     * Compares user input phone number against a stored phone number
     *
     * Handles Australian local format (0x) when stored number is Australian
     * and the trailing digits are unique across all stored numbers.
     * All other numbers must be entered in full international format.
     *
     * @param input phone number typed by the user
     * @param stored phone number stored in Room for this user
     * @param allStoredNumbers full list of all phone numbers in Room
     * @return true if both numbers refer to the same phone
     */
    fun phonesMatch(input: String, stored: String, allStoredNumbers: List<String>): Boolean =
        normalisePhone(input, stored, allStoredNumbers) == stored.filter { it.isDigit() }

    /**
     * Checks if a local 0x format input is ambiguous across all stored numbers
     *
     * @param inputDigits raw digits from user input starting with 0
     * @param allStoredNumbers full list of phone numbers from Room
     * @return true if the 0x input could conflict with a non-Australian number
     */
    fun isAmbiguous(inputDigits: String, allStoredNumbers: List<String>): Boolean {
        val converted = "61" + inputDigits.drop(1)
        val trailing = inputDigits.drop(1)
        return allStoredNumbers
            .map { it.filter { c -> c.isDigit() } }
            .any { stored -> stored != converted && stored.endsWith(trailing) }
    }

    /**
     * Normalises a phone number for storage in Room
     *
     * Strips all non-digit characters to ensure consistent storage format.
     * Converts local Australian 0x format to full 61x format.
     * All other international formats (e.g. +84, +1) are stripped of +
     * and stored as-is since they're already in full international form.
     *
     * Examples:
     * - 0436567330   → 61436567330  (Australian local)
     * - +61436567330 → 61436567330  (Australian international)
     * - 61436567330  → 61436567330  (Australian full)
     * - +84123456789 → 84123456789  (Vietnamese international)
     * - +12025551234 → 12025551234  (US international)
     *
     * @param phone raw phone number string from user input
     * @return clean numeric string ready for DB storage
     */
    fun normaliseForStorage(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return when {
            // Local Australian 0x — convert to 61x
            digits.startsWith("0") && digits.length == 10 -> "61" + digits.drop(1)
            // All other cases — digits only, no conversion
            else -> digits
        }
    }
}