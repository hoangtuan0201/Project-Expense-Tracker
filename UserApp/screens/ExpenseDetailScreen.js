import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';

export default function ExpenseDetailScreen({ route }) {
  const { expense } = route.params;

  return (
    <ScrollView style={styles.container}>
      <View style={styles.card}>
        <View style={styles.header}>
          <Text style={styles.expenseCode}>{expense.expenseCode}</Text>
          <Text style={[
            styles.statusBadge, 
            expense.paymentStatus === 'Paid' ? styles.statusPaid : 
            expense.paymentStatus === 'Reimbursed' ? styles.statusReimbursed : styles.statusPending
          ]}>
            {expense.paymentStatus}
          </Text>
        </View>

        <Text style={styles.amount}>{expense.amount} {expense.currency}</Text>
        <Text style={styles.type}>{expense.type}</Text>

        <View style={styles.divider} />

        <View style={styles.infoRow}>
          <Text style={styles.label}>Date</Text>
          <Text style={styles.value}>{expense.date}</Text>
        </View>

        <View style={styles.infoRow}>
          <Text style={styles.label}>Payment Method</Text>
          <Text style={styles.value}>{expense.paymentMethod}</Text>
        </View>

        <View style={styles.infoRow}>
          <Text style={styles.label}>Claimant</Text>
          <Text style={styles.value}>{expense.claimant}</Text>
        </View>

        {expense.location ? (
          <View style={styles.infoRow}>
            <Text style={styles.label}>Location</Text>
            <Text style={styles.value}>{expense.location}</Text>
          </View>
        ) : null}

        {expense.description ? (
          <View style={styles.descriptionContainer}>
            <Text style={styles.label}>Description</Text>
            <Text style={styles.descriptionText}>{expense.description}</Text>
          </View>
        ) : null}

        <View style={styles.divider} />
        
        <View style={styles.timestampContainer}>
          {expense.createdAt && <Text style={styles.timestamp}>Created: {expense.createdAt}</Text>}
          {expense.updatedAt && <Text style={styles.timestamp}>Updated: {expense.updatedAt}</Text>}
        </View>

      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F7FA',
    padding: 16,
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.05,
    shadowRadius: 10,
    elevation: 3,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  expenseCode: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#6200EE',
    backgroundColor: '#F0E6FF',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 6,
    fontFamily: 'Roboto',
  },
  statusBadge: {
    fontSize: 12,
    fontWeight: 'bold',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    overflow: 'hidden',
    fontFamily: 'Roboto',
  },
  statusPaid: { backgroundColor: '#E8F5E9', color: '#388E3C' },
  statusPending: { backgroundColor: '#FFF3E0', color: '#F57C00' },
  statusReimbursed: { backgroundColor: '#E3F2FD', color: '#1976D2' },
  amount: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 4,
    fontFamily: 'Roboto',
  },
  type: {
    fontSize: 16,
    color: '#666',
    fontWeight: '500',
    marginBottom: 20,
    fontFamily: 'Roboto',
  },
  divider: {
    height: 1,
    backgroundColor: '#E0E0E0',
    marginVertical: 16,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 12,
    alignItems: 'flex-start',
  },
  label: {
    fontSize: 15,
    color: '#888',
    fontWeight: '500',
    fontFamily: 'Roboto',
  },
  value: {
    fontSize: 15,
    color: '#333',
    fontWeight: '600',
    flex: 1,
    textAlign: 'right',
    marginLeft: 16,
    fontFamily: 'Roboto',
  },
  descriptionContainer: {
    marginTop: 4,
  },
  descriptionText: {
    fontSize: 15,
    color: '#444',
    marginTop: 8,
    fontStyle: 'italic',
    lineHeight: 22,
    fontFamily: 'Roboto',
  },
  timestampContainer: {
    marginTop: 8,
  },
  timestamp: {
    fontSize: 12,
    color: '#AAA',
    marginBottom: 4,
    fontFamily: 'Roboto',
  }
});
