import 'package:flutter/material.dart';
import 'package:notes_app/core/init/dependency_injection.dart';
import 'package:notes_app/core/theme/app_theme.dart';

class NotesPage extends StatelessWidget {
  const NotesPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mis Notas')),
      body: const Center(child: Text('Panel de notas - en desarrollo')),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _addNote(context),
        tooltip: 'Añadir nota',
        child: const Icon(Icons.add),
      ),
    );
  }

  void _addNote(BuildContext context) {
    // TODO: Implementar diálogo de creación de nota
  }
}