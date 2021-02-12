package com.example.colortooth.utilities

import kotlin.math.abs
import kotlin.math.log10

// Convertir une valeur hexadécimale en entier
fun hexToInt(val1: Char, val2: Char) : Int {
    // Convertir le caractère 1 en entier
    var int1 = parseAscii(val1.toInt())
    // Convertir le caractère 2 en entier
    var int2 = parseAscii(val2.toInt())
    // Renvoie une valeur entière
    return (int1 * 16 + int2)
}

// Convertir un caractère ascii en entier
fun parseAscii(value: Int): Int{
    // Si char est de A à F
    return if(value > 64){
        // Ajuster la valeur de manière appropriée
        (value - 55)
        // Si char est de 0 a 9
    } else {
        // Ajuster la valeur de manière appropriée
        (value - 48)
    }
}

// Fonction pour obtenir le nombre de chiffres dans un entier
fun Int.length() = when(this) {
    // Si l'entier est 0
    0 -> 1
    // Sinon, renvoie le nombre de chiffres
    else -> log10(abs(toDouble())).toInt() + 1
}