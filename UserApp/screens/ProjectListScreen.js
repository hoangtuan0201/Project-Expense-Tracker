import React, { useState, useEffect, useLayoutEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, TextInput, ActivityIndicator, Image, ScrollView, Alert } from 'react-native';
import { database } from '../firebaseConfig';
import { ref, onValue, set } from 'firebase/database';
import { Ionicons } from '@expo/vector-icons';
import { CommonActions } from '@react-navigation/native';

export default function ProjectListScreen({ route, navigation }) {
  const { username } = route.params;
  const [projects, setProjects] = useState([]);
  const [filteredProjects, setFilteredProjects] = useState([]);
  const [favorites, setFavorites] = useState({});
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useLayoutEffect(() => {
    navigation.setOptions({
      headerRight: () => (
        <TouchableOpacity 
          style={{ marginRight: 16 }} 
          onPress={() => {
            Alert.alert(
              "Logout",
              "Are you sure you want to log out?",
              [
                { text: "Cancel", style: "cancel" },
                { 
                  text: "Logout", 
                  style: "destructive",
                  onPress: () => {
                    navigation.dispatch(
                      CommonActions.reset({
                        index: 0,
                        routes: [{ name: 'Login' }],
                      })
                    );
                  }
                }
              ]
            );
          }}
        >
          <Ionicons name="log-out-outline" size={24} color="#FF5252" />
        </TouchableOpacity>
      ),
      headerLeft: () => null, // Hide back button if any
    });
  }, [navigation]);

  useEffect(() => {
    const projectsRef = ref(database, `users/${username}/projects`);
    const favRef = ref(database, `users/${username}/favorites`);
    
    // Listen for real-time updates on projects
    const unsubscribeProjects = onValue(projectsRef, (snapshot) => {
      if (snapshot.exists()) {
        const data = snapshot.val();
        // Convert object to array
        const loadedProjects = Object.keys(data).map(key => ({
          firebaseId: key,
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

    // Listen for real-time updates on favorites
    const unsubscribeFavs = onValue(favRef, (snapshot) => {
      if (snapshot.exists()) {
        setFavorites(snapshot.val());
      } else {
        setFavorites({});
      }
    });

    return () => {
      unsubscribeProjects();
      unsubscribeFavs();
    };
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

  const toggleFavorite = (firebaseId) => {
    const isFav = !!favorites[firebaseId];
    const newFavs = { ...favorites };
    if (isFav) {
      delete newFavs[firebaseId];
    } else {
      newFavs[firebaseId] = true;
    }
    const favRef = ref(database, `users/${username}/favorites`);
    set(favRef, newFavs);
  };

  const favoriteProjects = projects.filter(p => favorites[p.firebaseId]);

  const renderFavoriteItem = (item) => (
    <TouchableOpacity 
      key={item.firebaseId}
      style={styles.favCard}
      onPress={() => navigation.navigate('ExpenseList', { project: item, username })}
      activeOpacity={0.8}
    >
      <Image 
        source={{ uri: item.photoUrl || 'https://via.placeholder.com/150' }} 
        style={styles.favImage} 
        resizeMode="cover" 
      />
      <View style={styles.favOverlay}>
        <Text style={styles.favTitle} numberOfLines={1}>{item.projectName}</Text>
        <Text style={styles.favCode}>{item.projectCode}</Text>
      </View>
    </TouchableOpacity>
  );

  const renderHeader = () => {
    if (favoriteProjects.length === 0) return null;
    return (
      <View style={styles.favSection}>
        <Text style={styles.sectionTitle}>🌟 Quick Access</Text>
        <ScrollView 
          horizontal 
          showsHorizontalScrollIndicator={false} 
          contentContainerStyle={styles.favScroll}
        >
          {favoriteProjects.map(renderFavoriteItem)}
        </ScrollView>
      </View>
    );
  };

  const renderProjectItem = ({ item }) => (
    <TouchableOpacity 
      style={styles.card}
      onPress={() => navigation.navigate('ExpenseList', { project: item, username })}
      activeOpacity={0.8}
    >
      <View style={styles.imageContainer}>
        {item.photoUrl ? (
          <Image source={{ uri: item.photoUrl }} style={styles.cardImage} resizeMode="cover" />
        ) : (
          <View style={styles.cardImagePlaceholder}>
            <Text style={styles.placeholderText}>No Image</Text>
          </View>
        )}
        <TouchableOpacity 
          style={styles.heartButton}
          onPress={(e) => {
            e.stopPropagation(); // Prevent opening the project
            toggleFavorite(item.firebaseId);
          }}
        >
          <Ionicons 
            name={favorites[item.firebaseId] ? "heart" : "heart-outline"} 
            size={22} 
            color={favorites[item.firebaseId] ? "#FF5252" : "#FFFFFF"} 
          />
        </TouchableOpacity>
      </View>
      
      <View style={styles.cardContent}>
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
          <Text style={styles.actionText}>VIEW EXPENSES &rarr;</Text>
        </View>
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <View style={styles.searchContainer}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search by project name or date..."
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
          ListHeaderComponent={renderHeader}
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
    zIndex: 10,
  },
  searchInput: {
    backgroundColor: '#F0F0F0',
    padding: 12,
    borderRadius: 8,
    fontSize: 16,
  },
  listContent: {
    padding: 16,
    paddingTop: 0,
  },
  favSection: {
    marginTop: 16,
    marginBottom: 8,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 12,
    fontFamily: 'Roboto',
  },
  favScroll: {
    paddingBottom: 16,
  },
  favCard: {
    width: 140,
    height: 100,
    borderRadius: 12,
    marginRight: 12,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 6,
    elevation: 4,
    backgroundColor: '#fff',
  },
  favImage: {
    width: '100%',
    height: '100%',
    position: 'absolute',
  },
  favOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.45)',
    justifyContent: 'flex-end',
    padding: 10,
  },
  favTitle: {
    color: '#fff',
    fontWeight: 'bold',
    fontSize: 14,
    marginBottom: 2,
    fontFamily: 'Roboto',
  },
  favCode: {
    color: '#rgba(255,255,255,0.8)',
    fontSize: 11,
    fontWeight: '600',
    fontFamily: 'Roboto',
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 16,
    marginBottom: 16,
    marginTop: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08,
    shadowRadius: 10,
    elevation: 3,
    overflow: 'hidden',
  },
  imageContainer: {
    position: 'relative',
  },
  cardImage: {
    width: '100%',
    height: 140,
  },
  cardImagePlaceholder: {
    width: '100%',
    height: 140,
    backgroundColor: '#E0E0E0',
    justifyContent: 'center',
    alignItems: 'center',
  },
  placeholderText: {
    color: '#888',
    fontWeight: '500',
    fontFamily: 'Roboto',
  },
  heartButton: {
    position: 'absolute',
    top: 12,
    right: 12,
    backgroundColor: 'rgba(0,0,0,0.3)',
    borderRadius: 20,
    width: 36,
    height: 36,
    justifyContent: 'center',
    alignItems: 'center',
  },
  cardContent: {
    padding: 16,
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
    borderRadius: 6,
    fontFamily: 'Roboto',
  },
  statusBadge: {
    fontSize: 12,
    fontWeight: 'bold',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    overflow: 'hidden',
    fontFamily: 'Roboto',
  },
  statusActive: { backgroundColor: '#E3F2FD', color: '#1976D2' },
  statusCompleted: { backgroundColor: '#E8F5E9', color: '#388E3C' },
  statusOnHold: { backgroundColor: '#FFF3E0', color: '#F57C00' },
  
  projectName: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
    fontFamily: 'Roboto',
  },
  projectDetails: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
    fontFamily: 'Roboto',
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
    fontFamily: 'Roboto',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingTop: 40,
  },
  emptyText: {
    fontSize: 16,
    color: '#999',
    fontFamily: 'Roboto',
  }
});
