import { shallowMount } from '@vue/test-utils'
import VueBootstrapTypeaheadListItem from '../../../js/components/typeahead/typeaheadListItem.vue'
import Vue from "vue";

describe('VueBootstrapTypeaheadListItem.vue', () => {
    let wrapper
    beforeEach(() => {
        wrapper = shallowMount(VueBootstrapTypeaheadListItem)
    })

    it('Mounts and renders an <a> tag', () => {
        expect(wrapper.exists()).toBe(true)
        expect(wrapper.contains('a')).toBe(true)
    })

    it('Renders textVariant classes properly', async () => {
        wrapper.setProps({textVariant: 'dark'})
        await Vue.nextTick();
        expect(wrapper.classes()).toEqual(expect.arrayContaining(['text-dark']))
    })

    it('Renders backgroundVariant classes properly', async () => {
        wrapper.setProps({backgroundVariant: 'light'})
        await Vue.nextTick();
        expect(wrapper.classes()).toEqual(expect.arrayContaining(['bg-light']))
    })

    it('Renders active class', async () => {
        wrapper.setProps({active: true})
        await Vue.nextTick();
        expect(wrapper.vm.active).toBe(true)
        expect(wrapper.classes()).toEqual(expect.arrayContaining(['active']))
    })
})