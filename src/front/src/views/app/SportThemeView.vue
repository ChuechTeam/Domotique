<script setup lang="ts">
import api, { AttributeType, CompleteUser, DeviceStat, DeviceStatsQueryFunction, DeviceStatsQueryGrouping, UserProfile } from '@/api';
import { formatAttribute } from '@/labels';
import { useAuthStore } from '@/stores/auth';
import { computed, onMounted, ref, watch } from 'vue';

const auth = useAuthStore()

// The attribute types we want to display in the leaderboard
const availableAttributes = [
    AttributeType.CaloriesBurned,
    AttributeType.ActivityDuration,
    AttributeType.Steps
];

// The currently selected attribute
const selectedAttribute = ref<AttributeType>(AttributeType.CaloriesBurned);

// The leaderboard data - array of stats with user details
const leaderboardData = ref<{ stat: DeviceStat, user: UserProfile | null }[]>([]);
const isLoading = ref(false);

// Format attribute names for the dropdown
const attributeLabels = {
    [AttributeType.CaloriesBurned]: "Calories brûlées",
    [AttributeType.ActivityDuration]: "Durée d'activité",
    [AttributeType.Steps]: "Nombre de pas"
};

// Load the leaderboard data based on the selected attribute
async function loadLeaderboard() {
    isLoading.value = true;
    try {
        // Get stats grouped by user, summing the selected attribute
        const statsResponse = await api.devices.getDeviceStats({
            deviceStatsQuery: {
                grouping: DeviceStatsQueryGrouping.User,
                _function: DeviceStatsQueryFunction.Sum,
                attribute: selectedAttribute.value,
                ascendingOrder: false
            }
        });

        api.userEvents.reportSportRankingsCheck();

        if (!statsResponse || !statsResponse.stats) {
            leaderboardData.value = [];
            return;
        }

        const stats = statsResponse.stats;
        for (let i = stats.length - 1; i >= 0; i--) {
            if (stats[i].group == null) {
                stats.splice(i, 1);
            }
        }

        // Extract user IDs from the stats
        const userIds = stats
            .map(stat => Number(stat.group))
            .filter(id => !isNaN(id));

        if (userIds.length === 0) {
            leaderboardData.value = [];
            return;
        }

        // Get user details for the IDs
        const usersResponse = await api.users.searchUsers({ ids: userIds });

        // Create a map of user IDs to user objects
        const userMap = new Map<number, UserProfile>();
        usersResponse.profiles.forEach(user => userMap.set(user.id, user));

        // Map the stats to include the user details
        leaderboardData.value = stats.map(stat => {
            const userId = Number(stat.group);
            return {
                stat,
                user: userMap.get(userId) || null
            };
        });
    } catch (error) {
        console.error("Error loading leaderboard data:", error);
        leaderboardData.value = [];
    } finally {
        isLoading.value = false;
    }
}

// Format the stat value with the appropriate units
function formatStatValue(stat: DeviceStat) {
    return formatAttribute(selectedAttribute.value, stat.value);
}

// Get medal emoji for top 3 positions
function getMedal(index: number) {
    switch (index) {
        case 0: return "🥇";
        case 1: return "🥈";
        case 2: return "🥉";
        default: return "";
    }
}

// Watch for changes to the selected attribute and reload the leaderboard
watch(selectedAttribute, () => {
    loadLeaderboard();
});

// Load the leaderboard on component mount
onMounted(() => {
    loadLeaderboard();
});
</script>

<template>
    <div class="sport-theme">
        <div class="container-lg">
            <h1 class="sub-page-title">Compétition sportive</h1>

            <div class="attribute-selector">
                <label for="attribute-select">Sélectionnez une donnée :</label>
                <Select id="attribute-select" v-model="selectedAttribute" :options="availableAttributes"
                    optionLabel="none" :placeholder="attributeLabels[selectedAttribute]" class="w-full md:w-14rem">
                    <template #value="slotProps">
                        {{ attributeLabels[slotProps.value] }}
                    </template>
                    <template #option="slotProps">
                        {{ attributeLabels[slotProps.option] }}
                    </template>
                </Select>
            </div>

            <div>
                <ProgressSpinner v-if="isLoading" class="spinner" />

                <div v-else-if="leaderboardData.length === 0" class="no-data">
                    <i class="pi pi-info-circle"></i>
                    <p>Aucune donnée disponible pour cette métrique</p>
                </div>

                <div v-else class="leaderboard">
                    <div class="leaderboard-header">
                        <div class="rank">#</div>
                        <div class="name">Utilisateur</div>
                        <div class="score">Score</div>
                    </div>

                    <div v-for="(item, index) in leaderboardData" :key="index" class="leaderboard-item"
                        :class="{ '-me': item.user.id === auth.userId }">
                        <div class="rank">
                            {{ index + 1 }}
                            <span class="medal">{{ getMedal(index) }}</span>
                        </div>
                        <div class="name">
                            <template v-if="item.user">
                                <RouterLink :to="{ name: 'profile', params: { userId: item.user.id } }"
                                    class="user-link">
                                    {{ item.user.firstName }} {{ item.user.lastName }}
                                </RouterLink>
                            </template>
                            <template v-else>
                                Utilisateur #{{ item.stat.group }}
                            </template>
                        </div>
                        <div class="score">{{ formatStatValue(item.stat) }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.sport-theme {
    display: flex;
    flex-direction: column;
    height: 100%;
    background-color: rgb(229, 237, 255);
}

.sub-page-title {
    text-align: center;
    margin-bottom: 2rem;
    /* color: rgb(38, 22, 126); */
}

.attribute-selector {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    max-width: 400px;
    margin: 0 auto 2rem auto;
}

.spinner {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
}

.no-data {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 200px;
    color: #888;
    gap: 1rem;
}

.no-data i {
    font-size: 2rem;
}

.leaderboard {
    display: flex;
    flex-direction: column;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid rgb(38, 22, 126);
}

.leaderboard-header {
    display: flex;
    padding: 1rem;
    font-weight: bold;
    border-bottom: 2px solid #eee;
    background-color: rgb(240, 244, 255);
}

.leaderboard-item {
    display: flex;
    padding: 1rem;
    border-bottom: 1px solid #eee;
    background-color: rgb(227, 223, 255);
}

.leaderboard-item:nth-child(even) {
    background-color: rgb(248, 250, 255);
}

.leaderboard-item.-me {
    background-color: rgb(36, 14, 160);
    color: white;

    & .user-link, & .score {
        color: white;
    }
}

.leaderboard-item:last-child {
    border-bottom: none;
}

.rank,
.name,
.score {
    padding: 0.5rem;
}

.rank {
    width: 80px;
    text-align: center;
    font-weight: bold;
}

.medal {
    margin-left: 0.5rem;
}

.name {
    flex: 1;
}

.score {
    width: 150px;
    text-align: right;
    font-weight: bold;
    color: rgb(38, 22, 126);
}

.user-link {
    color: rgb(38, 22, 126);
    text-decoration: none;
}

.user-link:hover {
    text-decoration: underline;
}

@media (max-width: 768px) {
    .container-lg {
        padding: 1rem;
    }

    .leaderboard-container {
        padding: 1rem;
    }

    .rank {
        width: 75px;
    }

    .score {
        width: 120px;
    }
}
</style>