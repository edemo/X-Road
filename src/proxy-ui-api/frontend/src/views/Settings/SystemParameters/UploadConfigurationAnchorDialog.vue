<template>
  <v-dialog :value="showPreview" persistent max-width="850">
    <template v-slot:activator="{ on }">
      <input
        v-show="false"
        ref="anchorUpload"
        type="file"
        accept=".xml"
        @change="onUploadFileChanged"
      />
      <large-button
        data-test="system-parameters-configuration-anchor-upload-button"
        outlined
        @click="$refs.anchorUpload.click()"
        :requires-permission="permissions.UPLOAD_ANCHOR"
        class="ml-5"
      >
        {{ $t('systemParameters.configurationAnchor.action.upload.button') }}
      </large-button>
    </template>
    <v-card class="xrd-card">
      <v-card-title>
        <span data-test="dialog-title" class="headline">{{
          $t('systemParameters.configurationAnchor.action.upload.dialog.title')
        }}</span>
      </v-card-title>
      <v-card-text class="content-wrapper">
        <v-container>
          <v-row class="mb-5">
            <v-col>
              {{
                $t(
                  'systemParameters.configurationAnchor.action.upload.dialog.info',
                )
              }}
            </v-col>
          </v-row>
          <v-row no-gutters>
            <v-col class="font-weight-bold" cols="12" sm="3">
              {{
                $t(
                  'systemParameters.configurationAnchor.action.upload.dialog.field.hash',
                )
              }}
            </v-col>
            <v-col cols="12" sm="9">
              {{ anchorPreview.hash | colonize }}
            </v-col>
          </v-row>
          <v-row no-gutters>
            <v-col class="font-weight-bold" cols="12" sm="3">
              {{
                $t(
                  'systemParameters.configurationAnchor.action.upload.dialog.field.generated',
                )
              }}
            </v-col>
            <v-col cols="12" sm="9">
              {{ anchorPreview.created_at | formatDateTime }}
            </v-col>
          </v-row>
          <v-row class="mt-5">
            <v-col>
              {{
                $t(
                  'systemParameters.configurationAnchor.action.upload.dialog.confirmation',
                )
              }}
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions class="xrd-card-actions">
        <v-spacer></v-spacer>
        <large-button
          data-test="system-parameters-upload-configuration-anchor-dialog-cancel-button"
          outlined
          @click="close"
          >{{ $t('action.cancel') }}</large-button
        >
        <large-button
          data-test="system-parameters-upload-configuration-anchor-dialog-confirm-button"
          @click="uploadAnchor"
          :loading="uploading"
          >{{ $t('action.confirm') }}</large-button
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue';
import LargeButton from '@/components/ui/LargeButton.vue';
import { Permissions } from '@/global';
import * as api from '@/util/api';
import { Anchor } from '@/types';

const EmptyAnchorPreview: Anchor = {
  hash: '',
  created_at: '',
};

export default Vue.extend({
  name: 'UploadConfigurationAnchorDialog',
  components: {
    LargeButton,
  },
  data() {
    return {
      uploading: false,
      anchorPreview: EmptyAnchorPreview,
      uploadedFile: null as string | ArrayBuffer | null,
      showPreview: false,
      permissions: Permissions,
    };
  },
  methods: {
    onUploadFileChanged(event: any): void {
      const fileList = (event.target.files ||
        event.dataTransfer.files) as FileList;
      if (!fileList.length) {
        return;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        if (!e?.target?.result) {
          return;
        }
        api
          .post('/system/anchor/preview', e.target.result, {
            headers: {
              'Content-Type': 'application/octet-stream',
            },
          })
          .then((resp) => {
            this.uploadedFile = e.target!.result;
            this.anchorPreview = resp.data;
            this.showPreview = true;
          })
          .catch((error) => this.$store.dispatch('showError', error));
      };
      reader.readAsArrayBuffer(fileList[0]);
    },
    uploadAnchor(): void {
      this.uploading = true;
      api
        .post('/system/anchor', this.uploadedFile, {
          headers: {
            'Content-Type': 'application/octet-stream',
          },
        })
        .catch((error) => this.$store.dispatch('showError', error))
        .finally(() => {
          this.uploading = false;
          this.close();
          this.$store.dispatch(
            'showSuccess',
            'systemParameters.configurationAnchor.action.upload.dialog.success',
          );
          this.$emit('uploaded');
        });
    },
    close(): void {
      this.showPreview = false;
      this.anchorPreview = EmptyAnchorPreview;
    },
  },
});
</script>

<style scoped lang="scss">
@import '../../../assets/dialogs';
</style>
