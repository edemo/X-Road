<template>
  <v-layout
    align-center
    justify-center
    column
    fill-height
    elevation-0
    class="data-table-wrapper xrd-view-common"
  >
    <div class="table-toolbar">
      <v-text-field
        v-model="search"
        :label="$t('action.search')"
        data-test="search-clients-input"
        single-line
        hide-details
        class="search-input"
      >
        <v-icon slot="append">mdi-magnify</v-icon>
      </v-text-field>
      <v-btn
        v-if="showAddClient()"
        color="primary"
        @click="addClient"
        data-test="add-client-button"
        rounded
        dark
        class="ma-0 rounded-button elevation-0"
      >{{$t('action.addClient')}}</v-btn>
    </div>

    <v-data-table
      :loading="loading"
      :headers="headers"
      :items="clients"
      :search="search"
      :must-sort="true"
      :items-per-page="-1"
      :sort-by="['sortNameAsc']"
      :custom-sort="customSort"
      :custom-filter="customFilter"
      hide-default-footer
      class="elevation-0 data-table"
      item-key="id"
    >
      <template v-slot:item.sortNameAsc="{ item }">
        <!-- Name - Owner member -->
        <template v-if="item.type == 'owner'">
          <v-icon color="grey darken-2" class="icon-member icon-size">mdi-folder-open</v-icon>
          <span
            v-if="canOpenClient()"
            class="font-weight-bold name clickable"
            @click="openClient(item)"
          >{{item.name}} ({{ $t("client.owner") }})</span>

          <span v-else class="font-weight-bold name">{{item.name}} ({{ $t("client.owner") }})</span>
        </template>
        <!-- Name - member -->
        <template v-else-if="item.type == 'client'">
          <v-icon color="grey darken-2" class="icon-member icon-size">mdi-folder-open-outline</v-icon>
          <span class="font-weight-bold name-member">{{item.name}}</span>
        </template>
        <!-- Name - Subsystem -->
        <template v-else>
          <v-icon
            color="grey darken-2"
            class="icon-member icon-size"
            :class="{ 'icon-subsystem': treeMode }"
          >mdi-card-bulleted-outline</v-icon>
          <span
            v-if="canOpenClient()"
            class="font-weight-bold name clickable"
            @click="openSubsystem(item)"
          >{{item.name}}</span>
          <span v-else class="font-weight-bold name">{{item.name}}</span>
        </template>
      </template>

      <template v-slot:item.status="{ item }">
        <client-status :status="item.status" />
      </template>

      <template v-slot:item.button="{ item }">
        <div class="button-wrap">
          <v-btn
            v-if="(item.type == 'client' ||item.type == 'owner') && showAddClient()"
            small
            outlined
            rounded
            color="primary"
            class="xrd-small-button xrd-table-button"
            @click="addSubsystem(item)"
          >{{$t('action.addSubsystem')}}</v-btn>
        </div>
      </template>

      <template slot="no-data">{{$t('action.noData')}}</template>
      <v-alert
        slot="no-results"
        :value="true"
        color="error"
      >{{ $t('action.emptySearch', { msg: search }) }}</v-alert>
    </v-data-table>
  </v-layout>
</template>

<script lang="ts">
/**
 * This component renders the Clients data table.
 * Default sort and filter functions are replaced to achieve the end result where
 */
import Vue from 'vue';
import ClientStatus from './ClientStatus.vue';
import { mapGetters } from 'vuex';
import { Permissions, RouteName } from '@/global';

export default Vue.extend({
  components: {
    ClientStatus,
  },

  data: () => ({
    search: '',
    pagination: {
      sortBy: 'sortNameAsc',
    },
  }),

  computed: {
    ...mapGetters(['clients', 'loading']),
    treeMode(): boolean {
      // Switch between the "tree" view and the "flat" view
      if (this.search) {
        return false;
      } else if (this.pagination.sortBy === 'status') {
        return false;
      }
      return true;
    },
    headers(): any[] {
      return [
        {
          text: this.$t('client.name'),
          align: 'left',
          value: 'sortNameAsc',
          class: 'xrd-table-header',
        },
        {
          text: this.$t('client.id'),
          align: 'left',
          value: 'id',
          class: 'xrd-table-header',
        },
        {
          text: this.$t('client.status'),
          align: 'left',
          value: 'status',
          class: 'xrd-table-header',
        },
        {
          text: '',
          value: 'button',
          sortable: false,
          class: 'xrd-table-header',
        },
      ];
    },
  },

  methods: {
    showAddClient(): boolean {
      return this.$store.getters.hasPermission(Permissions.ADD_CLIENT);
    },
    canOpenClient(): boolean {
      return this.$store.getters.hasPermission(Permissions.VIEW_CLIENT_DETAILS);
    },

    openClient(item: any): void {
      this.$router.push({
        name: RouteName.Client,
        params: { id: item.id },
      });
    },

    openSubsystem(item: any): void {
      this.$router.push({
        name: RouteName.Subsystem,
        params: { id: item.id },
      });
    },

    addClient(): void {
      this.$router.push({
        name: RouteName.AddClient,
      });
    },

    addSubsystem(item: any): void {
      this.$router.push({
        name: RouteName.AddSubsystem,
      });
    },

    customFilter: (value: any, search: string | null, item: any): boolean => {
      // Override for the default filter function.
      // This is done to filter by the name (that is visible to user) instead of sortNameAsc or sortNameDesc.
      if (search === null) {
        return true;
      }

      search = search.toString().toLowerCase();
      if (search.trim() === '') {
        return true;
      }

      if (
        item.name.toLowerCase().includes(search) ||
        item.id.toLowerCase().includes(search)
      ) {
        return true;
      }

      return false;
    },

    customSort(items: any[], sortBy: string[], sortDesc: boolean[]): any[] {
      // Override of the default sorting function for the Name column to use sortNameAsc or sortNameDesc instead.
      // This is needed to achieve the order where member is always over the subsystem regardless of the sort direction.
      const index = sortBy[0];
      const isDesc = sortDesc[0];

      items.sort((a, b) => {
        if (index === 'sortNameAsc') {
          if (!isDesc) {
            return a[index] < b[index] ? -1 : 1;
          } else {
            // When sorting descending by name, replace the sort data
            return b.sortNameDesc < a.sortNameDesc ? -1 : 1;
          }
        } else {
          if (!isDesc) {
            return a[index] < b[index] ? -1 : 1;
          } else {
            return b[index] < a[index] ? -1 : 1;
          }
        }
      });
      return items;
    },
  },
});
</script>

<style lang="scss">
.xrd-table-header {
  border-bottom: 1px solid #9c9c9c !important;
}
</style>

<style lang="scss" scoped>
.icon-member {
  padding-left: 0;
}

.icon-subsystem {
  padding-left: 40px;
}

.icon-size {
  font-size: 20px;
}

.table-toolbar {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-end;
  width: 100%;
  padding-left: 24px;
  margin-bottom: 24px;
}

.search-input {
  max-width: 300px;
}

.data-table-wrapper {
  width: 100%;
}

.data-table {
  width: 100%;
}

.name {
  margin-left: 14px;
  margin-top: auto;
  margin-bottom: auto;
  text-align: center;

  &.clickable {
    text-decoration: underline;
    cursor: pointer;
  }
}

.name-member {
  margin-left: 14px;
  margin-top: auto;
  margin-bottom: auto;
  text-align: center;
}

.button-wrap {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}
</style>
