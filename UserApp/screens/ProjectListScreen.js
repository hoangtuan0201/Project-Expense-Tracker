import React, { useState, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, TextInput, ActivityIndicator } from 'react-native';
import { database } from '../firebaseConfig';
import { ref, onValue } from 'firebase/database';

export default function ProjectListScreen({ route, navigation }) {
  const { username } = route.params;
  const [projects, setProjects] = useState([]);
  const [filteredProjects, setFilteredProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const projectsRef = ref(database, `users/${username}/projects`);
    
    // Listen for real-time updates
    const unsubscribe = onValue(projectsRef, (snapshot) => {
      if (snapshot.exists()) {
        const data = snapshot.val();
        // Convert object to array
        const loadedProjects = Object.keys(data).map(key => ({
          firebaseId: key, // Keep track of the node key to add expenses later
          ...data[key]
        }));
        // Sort by start date or id
        loadedProjects.sort((a, b) => (b.id || 0) - (a.id || 0));
        setProjects(loadedProjects);
        setFilteredProjects(loadedProjects);
      } else {
        setProjects([]);
        setFilteredProjects([]);
      }
      setLoading(false);
    }, (error) => {
      console.error("Firebase fetch error:", error);
      setLoading(false);
    });

    return () => unsubscribe();
  }, [username]);

  useEffect(() => {
    if (searchQuery.trim() === '') {
      setFilteredProjects(projects);
    } else {
      const lowerQuery = searchQuery.toLowerCase();
      const filtered = projects.filter(project => {
        const nameMatch = project.projectName && project.projectName.toLowerCase().includes(lowerQuery);
        const dateMatch = project.startDate && project.startDate.includes(searchQuery);
        return nameMatch || dateMatch;
      });
      setFilteredProjects(filtered);
    }
  }, [searchQuery, projects]);

  const renderProjectItem = ({ item }) => (
    <TouchableOpacity 
      style={styles.card}
      onPress={() => navigation.navigate('AddExpense', { project: item, username })}
    >
      <View style={styles.cardHeader}>
        <Text style={styles.projectCode}>{item.projectCode || 'N/A'}</Text>
        <Text style={[
          styles.statusBadge, 
          item.status === 'Completed' ? styles.statusCompleted : 
          item.status === 'On Hold' ? styles.statusOnHold : styles.statusActive
        ]}>
          {item.status || 'Active'}
        </Text>
      </View>
      <Text style={styles.projectName}>{item.projectName || 'Unnamed Project'}</Text>
      <Text style={styles.projectDetails}>Manager: {item.manager || 'N/A'}</Text>
      <Text style={styles.projectDetails}>Date: {item.startDate || 'N/A'} to {item.endDate || 'N/A'}</Text>
      
      <View style={styles.actionContainer}>
        <Text style={styles.actionText}>+ ADD EXPENSE</Text>
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <View style={styles.searchContainer}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search by project name or date (YYYY-MM-DD)"
          value={searchQuery}
          onChangeText={setSearchQuery}
        />
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#6200EE" style={styles.loader} />
      ) : filteredProjects.length > 0 ? (
        <FlatList
          data={filteredProjects}
          keyExtractor={(item) => item.firebaseId || item.id?.toString() || Math.random().toString()}
          renderItem={renderProjectItem}
          contentContainerStyle={styles.listContent}
        />
      ) : (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyText}>No projects found.</Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F7FA',
  },
  loader: {
    marginTop: 40,
  },
  searchContainer: {
    padding: 16,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#E0E0E0',
  },
  searchInput: {
    backgroundColor: '#F0F0F0',
    padding: 12,
    borderRadius: 8,
    fontSize: 16,
  },
  listContent: {
    padding: 16,
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  projectCode: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#6200EE',
    backgroundColor: '#F0E6FF',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 4,
  },
  statusBadge: {
    fontSize: 12,
    fontWeight: 'bold',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    overflow: 'hidden',
  },
  statusActive: { backgroundColor: '#E3F2FD', color: '#1976D2' },
  statusCompleted: { backgroundColor: '#E8F5E9', color: '#388E3C' },
  statusOnHold: { backgroundColor: '#FFF3E0', color: '#F57C00' },
  
  projectName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  projectDetails: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
  },
  actionContainer: {
    marginTop: 12,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#F0F0F0',
    alignItems: 'flex-end',
  },
  actionText: {
    color: '#6200EE',
    fontWeight: 'bold',
    fontSize: 14,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 16,
    color: '#999',
  }
});
