import 'package:flutter/material.dart';

class AppTheme {
  static const Color primary = Color(0xFF2563EB);       // Blue-600
  static const Color primaryDark = Color(0xFF1D4ED8);   // Blue-700
  static const Color accent = Color(0xFF10B981);        // Emerald-500
  static const Color surface = Color(0xFFF9FAFB);       // Gray-50
  static const Color error = Color(0xFFEF4444);         // Red-500
  static const Color textPrimary = Color(0xFF111827);   // Gray-900
  static const Color textSecondary = Color(0xFF6B7280); // Gray-500

  static ThemeData get lightTheme => ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: primary,
          primary: primary,
          surface: Colors.white,
          error: error,
        ),
        useMaterial3: true,
        fontFamily: 'Roboto',
        scaffoldBackgroundColor: Colors.white,
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.white,
          foregroundColor: textPrimary,
          elevation: 0,
          centerTitle: false,
          titleTextStyle: TextStyle(
            color: textPrimary,
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
          iconTheme: IconThemeData(color: textPrimary),
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: primary,
            foregroundColor: Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.all(Radius.circular(12)),
            ),
            padding: EdgeInsets.symmetric(vertical: 14, horizontal: 24),
          ),
        ),
        cardTheme: const CardThemeData(
          elevation: 1,
          margin: EdgeInsets.zero,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(12)),
            side: BorderSide(color: Color(0xFFE5E7EB)),
          ),
        ),
        inputDecorationTheme: InputDecorationTheme(
          border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
          filled: true,
          fillColor: surface,
        ),
        tabBarTheme: const TabBarThemeData(
          dividerColor: Color(0xFFE5E7EB),
        ),
      );
}
