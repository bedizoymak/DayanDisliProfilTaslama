package com.dayandisli.calculator.domain

/** Ported from src/calculator/data/machines.ts — order and content must match exactly. */
data class Machine(
    val id: String,
    val name: String,
    val description: String,
)

val MACHINES: List<Machine> = listOf(
    Machine("kucuk-azdirma", "Küçük Azdırma", "Küçük modüllü dişliler için azdırma makinesi"),
    Machine("reishauer-taslama", "REISHAUER Taşlama", "Yüksek hassasiyetli profil taşlama makinesi"),
    Machine("yatay-azdirma", "Yatay Azdırma", "Yatay tip azdırma makinesi"),
    Machine("fellows-lorenz-snj5", "Fellows Makinesi Lorenz SNJ5", "Lorenz SNJ5 model Fellows makinesi"),
    Machine("pfauter-p403", "Azdırma Makinesi Pfauter P403", "Pfauter P403 model azdırma makinesi"),
    Machine("fellows-lorenz-s71", "Fellows Makinesi Lorenz S71", "Lorenz S71 model Fellows makinesi"),
    Machine("azdirma-genel", "Azdırma (Genel)", "Genel amaçlı azdırma makinesi"),
    Machine("torna-tezgahi", "Torna Tezgahı", "Dişli tornalama işlemleri için torna tezgahı"),
    Machine("silindirik-taslama", "Silindirik Taşlama Makinesi", "Silindirik yüzey taşlama makinesi"),
    Machine("gleason", "Gleason Makinesi", "Konik dişli üretimi için Gleason makinesi"),
    Machine("hurth", "Hurth Makinesi", "Hurth marka dişli makinesi"),
    Machine("liebherr", "Liebherr Makinesi", "Liebherr marka hassas dişli makinesi"),
)

fun getMachineById(id: String): Machine? = MACHINES.find { it.id == id }
