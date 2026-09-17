import 'package:flutter/services.dart';
import 'package:notes_app/core/constants/app_constants.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

final serviceLocator = GetIt.instance;

Future<void> init_di() async {
  // Supabase initialization
  await Supabase.initialize(
    apiKey: AppConstants.supabaseAnonKey,
    url: AppConstants.supabaseUrl,
  );

  // Register services, repos, datasources...
}