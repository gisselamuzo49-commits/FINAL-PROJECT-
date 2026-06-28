import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://fvflivypuybajxbrqjwm.supabase.co'
const supabaseKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZ2ZmxpdnlwdXliYWp4YnJxandtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI2NjY5NzgsImV4cCI6MjA5ODI0Mjk3OH0.h0RggJb-ausBUUeqMRzBg1WddUtyla7T9YaQKLC5axM'

export const supabase = createClient(supabaseUrl, supabaseKey)
