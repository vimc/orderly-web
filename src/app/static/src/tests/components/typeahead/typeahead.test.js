import { mount } from '@vue/test-utils'
import VueBootstrapTypeahead from '../../../js/components/typeahead/typeahead.vue'
import VueBootstrapTypeaheadList from '../../../js/components/typeahead/typeaheadList.vue'
import Vue from "vue";

describe('VueBootstrapTypeahead', () => {
    let wrapper

    const demoData = [
        'Canada',
        'United States',
        'Mexico',
        'Japan',
        'China',
        'United Kingdom'
    ]

    beforeEach(() => {
        wrapper = mount(VueBootstrapTypeahead, {
            propsData: {
                data: demoData
            }
        })
    })

    it('Should mount and render a hidden typeahead list', () => {
        let child = wrapper.findComponent(VueBootstrapTypeaheadList)
        expect(child).toBeTruthy()
        expect(child.isVisible()).toBe(false)
    })

    it('Formats the input data properly', () => {
        expect(wrapper.vm.formattedData[0].id).toBe(0)
        expect(wrapper.vm.formattedData[0].data).toBe('Canada')
        expect(wrapper.vm.formattedData[0].text).toBe('Canada')
    })

    it('Uses a custom serializer properly', async () => {
        wrapper.setProps({
            data: [{
                name: 'Canada',
                code: 'CA'
            }],
            value: 'Can',
            serializer: t => t.name
        })
        await Vue.nextTick();
        expect(wrapper.vm.formattedData[0].id).toBe(0)
        expect(wrapper.vm.formattedData[0].data.code).toBe('CA')
        expect(wrapper.vm.formattedData[0].text).toBe('Canada')
    })

    it('Show the list when given a query and focused', async () => {
        let child = wrapper.findComponent(VueBootstrapTypeaheadList)
        wrapper.find('input').setValue('Can')
        await Vue.nextTick();
        expect(child.isVisible()).toBe(false)
        wrapper.find('input').trigger('focus')
        await Vue.nextTick();
        expect(child.isVisible()).toBe(true)
    })

    it('Hides the list when blurred', async () => {
        let child = wrapper.findComponent(VueBootstrapTypeaheadList)
        wrapper.setData({inputValue: 'Can'})
        await Vue.nextTick();
        wrapper.find('input').trigger('focus')
        await Vue.nextTick();
        expect(child.isVisible()).toBe(true)
        wrapper.find('input').trigger('blur')
        await Vue.nextTick();
        expect(child.isVisible()).toBe(false)
    })

    it('Renders the list in different sizes', async() => {
        expect(wrapper.vm.sizeClasses).toBe('input-group')
        wrapper.setProps({
            size: 'lg'
        })
        await Vue.nextTick()
        expect(wrapper.vm.sizeClasses).toBe('input-group input-group-lg')
    })
})