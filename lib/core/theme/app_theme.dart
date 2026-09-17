import 'package:flutter/material.dart';

final appTheme = ThemeData(
  useMaterial3: true,
  brightness: Brightness.light,
  colorScheme: ColorScheme.light(
    primary: const Color(0xFF1976D2),
    secondary: const Color(0xFF028841),
    surface: Colors.white,
    onSurface: Colors.black87,
  ),
  scaffoldBackgroundColor: Colors.white,
  elevatedButtonTheme: ElevatedButtonThemeData(
    style: ElevatedButton.styleFrom(
      foregroundColor: Colors.white,
      backgroundColor: const Color(0xFF1976D2),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
      ),
    ),
  ),
  inputDecorationTheme: InputDecorationTheme(
    border: OutlineInputBorder(
      borderRadius: BorderRadius.circular(8),
    ),
    contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 16),
  ),
);