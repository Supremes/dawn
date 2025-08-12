import { defineStore } from 'pinia'
import { DawnDia, DiaConfig } from '@/utils/dawn-dia'

export const useDiaStore = defineStore('diaStore', {
  state: () => {
    return {
      dia: new DawnDia(),
      dawn_bot: {
        enable: true,
        locale: 'cn',
        bot_type: 'dia'
      }
    }
  },
  actions: {
    initializeBot(configs: DiaConfig): void {
      this.dia.installSoftware(configs)
      this.dia.on()
    }
  }
})
