import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ScrollView, KeyboardAvoidingView, Platform, Alert, ActivityIndicator } from 'react-native';
import { database } from '../firebaseConfig';
import { ref, push, set } from 'firebase/database';
import DateTimePicker from '@react-native-community/datetimepicker';

const EXPENSE_TYPES = ['Travel', 'Equipment', 'Materials', 'Services', 'Software/Licenses', 'Labour costs', 'Utilities', 'Miscellaneous'];
const PAYMENT_METHODS = ['Cash', 'Credit Card', 'Bank Transfer', 'Cheque'];
const PAYMENT_STATUSES = ['Pending', 'Paid', 'Reimbursed'];

export default function AddExpenseScreen({ route, navigation }) {
  const { project, username } = route.params;

  const [expenseCode, setExpenseCode] = useState(`EXP-${Math.floor(Math.random() * 10000)}`);
  const [date, setDate] = useState(new Date());
  const [showDatePicker, setShowDatePicker] = useState(false);
  
  const [amount, setAmount] = useState('');
  const [currency, setCurrency] = useState('USD');
  const [type, setType] = useState(EXPENSE_TYPES[0]);
  const [paymentMethod, setPaymentMethod] = useState(PAYMENT_METHODS[0]);
  const [paymentStatus, setPaymentStatus] = useState(PAYMENT_STATUSES[0]);
  const [claimant, setClaimant] = useState(username);
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [loading, setLoading] = useState(false);

  const onDateChange = (event, selectedDate) => {
    const currentDate = selectedDate || date;
    setShowDatePicker(Platform.OS === 'ios');
    setDate(currentDate);
  };

  const formatDate = (dateObj) => {
    return dateObj.toISOString().split('T')[0];
  };

  const handleSave = async () => {
    if (!amount || isNaN(amount)) {
      Alert.alert('Error', 'Please enter a valid amount');
      return;
    }

    setLoading(true);

    try {
      // The path to save this expense
      const projectKey = project.firebaseId;
      const expensesRef = ref(database, `users/${username}/projects/${projectKey}/expenses`);
      
      const newExpenseRef = push(expensesRef);
      
      const expenseData = {
        expenseCode,
        date: formatDate(date),
        amount: parseFloat(amount),
        currency,
        type,
        paymentMethod,
        claimant,
        paymentStatus,
        description,
        location
      };

      await set(newExpenseRef, expenseData);
      
      Alert.alert('Success', 'Expense added successfully!', [
        { text: 'OK', onPress: () => navigation.goBack() }
      ]);
    } catch (error) {
      Alert.alert('Error', 'Failed to save expense: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const renderPicker = (items, selectedItem, onSelect) => (
    <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.pickerContainer}>
      {items.map((item) => (
        <TouchableOpacity
          key={item}
          style={[styles.pickerItem, selectedItem === item && styles.pickerItemSelected]}
          onPress={() => onSelect(item)}
        >
          <Text style={[styles.pickerText, selectedItem === item && styles.pickerTextSelected]}>
            {item}
          </Text>
        </TouchableOpacity>
      ))}
    </ScrollView>
  );

  return (
    <KeyboardAvoidingView 
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.card}>
          <Text style={styles.projectTitle}>Adding to: {project.projectName}</Text>
          
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Expense Code</Text>
            <TextInput style={[styles.input, styles.disabledInput]} value={expenseCode} editable={false} />
          </View>

          <View style={styles.row}>
            <View style={[styles.inputGroup, { flex: 2, marginRight: 8 }]}>
              <Text style={styles.label}>Amount *</Text>
              <TextInput 
                style={styles.input} 
                keyboardType="numeric" 
                value={amount} 
                onChangeText={setAmount} 
                placeholder="0.00" 
              />
            </View>
            <View style={[styles.inputGroup, { flex: 1 }]}>
              <Text style={styles.label}>Currency</Text>
              <TextInput style={styles.input} value={currency} onChangeText={setCurrency} />
            </View>
          </View>

          <View style={styles.inputGroup}>
            <Text style={styles.label}>Date</Text>
            <TouchableOpacity style={styles.input} onPress={() => setShowDatePicker(true)}>
              <Text>{formatDate(date)}</Text>
            </TouchableOpacity>
            {showDatePicker && (
              <DateTimePicker
                value={date}
                mode="date"
                display="default"
                onChange={onDateChange}
              />
            )}
          </View>

          <View style={styles.inputGroup}>
            <Text style={styles.label}>Expense Type</Text>
            {renderPicker(EXPENSE_TYPES, type, setType)}
          </View>

          <View style={styles.inputGroup}>
            <Text style={styles.label}>Payment Method</Text>
            {renderPicker(PAYMENT_METHODS, paymentMethod, setPaymentMethod)}
          </View>

          <View style={styles.inputGroup}>
            <Text style={styles.label}>Payment Status</Text>
            {renderPicker(PAYMENT_STATUSES, paymentStatus, setPaymentStatus)}
          </View>

          <View style={styles.inputGroup}>
            <Text style={styles.label}>Claimant</Text>
            <TextInput style={styles.input} value={claimant} onChangeText={setClaimant} />
          </View>

          <View style={styles.inputGroup}>
            <Text style={styles.label}>Location</Text>
            <TextInput style={styles.input} value={location} onChangeText={setLocation} placeholder="Where was this expense made?" />
          </View>

          <View style={styles.inputGroup}>
            <Text style={styles.label}>Description</Text>
            <TextInput 
              style={[styles.input, styles.textArea]} 
              multiline 
              numberOfLines={3} 
              value={description} 
              onChangeText={setDescription} 
              placeholder="Additional details..." 
            />
          </View>

          <TouchableOpacity 
            style={styles.button} 
            onPress={handleSave}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="#fff" />
            ) : (
              <Text style={styles.buttonText}>SAVE EXPENSE</Text>
            )}
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F7FA',
  },
  scrollContent: {
    padding: 16,
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 12,
    elevation: 5,
  },
  projectTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#6200EE',
    marginBottom: 24,
    textAlign: 'center',
    fontFamily: 'Roboto',
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  inputGroup: {
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    color: '#333',
    marginBottom: 8,
    fontWeight: '600',
    fontFamily: 'Roboto',
  },
  input: {
    borderWidth: 1,
    borderColor: '#E0E0E0',
    borderRadius: 8,
    padding: 14,
    fontSize: 16,
    backgroundColor: '#FAFAFA',
    justifyContent: 'center',
    fontFamily: 'Roboto',
  },
  disabledInput: {
    backgroundColor: '#EEEEEE',
    color: '#999',
    fontFamily: 'Roboto',
  },
  textArea: {
    height: 100,
    textAlignVertical: 'top',
  },
  pickerContainer: {
    flexDirection: 'row',
  },
  pickerItem: {
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 20,
    backgroundColor: '#F0F0F0',
    marginRight: 8,
    borderWidth: 1,
    borderColor: 'transparent',
  },
  pickerItemSelected: {
    backgroundColor: '#F0E6FF',
    borderColor: '#6200EE',
  },
  pickerText: {
    color: '#666',
    fontFamily: 'Roboto',
  },
  pickerTextSelected: {
    color: '#6200EE',
    fontWeight: 'bold',
    fontFamily: 'Roboto',
  },
  button: {
    backgroundColor: '#6200EE',
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 24,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
    fontFamily: 'Roboto',
  },
});
