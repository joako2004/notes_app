import 'package:flutter/material.dart';
import 'package:notes_app/core/init/dependency_injection.dart';
import 'package:notes_app/core/theme/app_theme.dart';
import 'package:notes_app/features/notes/presentation/notes_page.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await init_di();
  runApp(const NotesApp());
}

class NotesApp extends StatelessWidget {
  const NotesApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Notes App',
      theme: appTheme,
      darkTheme: appTheme.copyWith(brightness: Brightness.dark),
      themeMode: ThemeMode.system,
      home const NotesPage(),
      navigatorKey: AppNavigator.navKey,
    );
  }
}