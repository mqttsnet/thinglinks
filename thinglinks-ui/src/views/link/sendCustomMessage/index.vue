<template>
  <div class="send-message-container">
    <el-form ref="form" :model="form" :rules="rules" label-width="200px" class="message-form">
      <el-form-item label="MQTT Topic:" prop="topic">
        <el-input v-model="form.topic" placeholder="Enter MQTT topic"></el-input>
      </el-form-item>
      <el-form-item label="QoS Level:" prop="qos">
        <el-select v-model="form.qos" placeholder="Select QoS level">
          <el-option label="QoS0 - At most once" value="0"></el-option>
          <el-option label="QoS1 - At least once" value="1"></el-option>
          <el-option label="QoS2 - Exactly once" value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="Tenant ID:" prop="tenantId">
        <el-select v-model="form.tenantId" placeholder="Select Tenant ID">
          <el-option
            v-for="app in dict.type.link_application_type"
            :key="app.value"
            :label="app.label"
            :value="app.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Expiry Seconds:" prop="expirySeconds">
        <el-input v-model="form.expirySeconds" placeholder="Duration in seconds"></el-input>
      </el-form-item>
      <el-form-item label="Metadata:" prop="metadata">
        <div v-for="(entry, index) in metadataEntries" :key="index" class="metadata-entry">
          <el-input v-model="entry.key" placeholder="Enter metadata key (without 'client_meta_' prefix)" class="metadata-key" @input="updateMetadata"></el-input>
          <el-input v-model="entry.value" placeholder="Value" class="metadata-value" @input="updateMetadata"></el-input>
          <el-button icon="el-icon-delete" @click="removeMetadataField(index)"></el-button>
        </div>
        <el-button @click="addMetadataField">Add Metadata</el-button>
      </el-form-item>
      <el-form-item label="Message:" prop="message">
        <el-input type="textarea" v-model="form.payload" placeholder="Enter message content"></el-input>
      </el-form-item>
      <el-form-item class="form-buttons">
        <el-button type="primary" @click="submitForm">Send Message</el-button>
        <el-button @click="resetForm">Reset</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {sendCustomMessage} from "@/api/link/deviceCommand";

export default {
  name: "sendCustomMessage",
  dicts: [
    "link_application_type",
  ],
  data() {
    return {
      form: {
        topic: '',
        qos: '',
        tenantId: '',
        payload: '',
        expirySeconds: '',
        metadata: {}
      },
      metadataEntries: [{key: '', value: ''}], // Start with one empty key-value pair
      rules: {
        topic: [{required: true, message: "Please enter MQTT topic", trigger: "blur"}],
        qos: [{required: true, message: "Please select QoS level", trigger: "change"}],
        tenantId: [{required: true, message: "Please select a Tenant ID", trigger: "change"}],
        payload: [{required: true, message: "Please enter message content", trigger: "blur"}],
      }
    };
  },
  methods: {
    addMetadataField() {
      this.metadataEntries.push({key: '', value: ''});
    },
    removeMetadataField(index) {
      this.metadataEntries.splice(index, 1);
      this.updateMetadata(); // Update the actual metadata object
    },
    updateMetadata() {
      // Transform the array of key-value pairs into an object
      let newMetadata = {};
      this.metadataEntries.forEach(entry => {
        if (entry.key && entry.value) {
          newMetadata[`client_meta_${entry.key}`] = entry.value;
        }
      });
      this.form.metadata = newMetadata;
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          sendCustomMessage(this.form).then(response => {
            if (response.isSuccess()) {
              this.$message.success("Message sent successfully");
            } else {
              this.$message.error(`Failed to send message: ${response.getMsg()}`);
            }
          });
        }
      });
    },
    resetForm() {
      if (this.$refs.form) {
        this.$refs.form.resetFields();
      }
      this.metadataEntries = [{ key: '', value: '' }];
      this.form.payload = '';
    }
  }
}
</script>


<style scoped>
.send-message-container {
  max-width: 600px;
  margin: 50px auto;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background: #fff;
  padding: 20px;
}

.metadata-entry {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.metadata-key, .metadata-value {
  flex: 1;
  margin-right: 10px;
}

.form-buttons {
  display: flex;
  justify-content: space-between;
}
</style>
