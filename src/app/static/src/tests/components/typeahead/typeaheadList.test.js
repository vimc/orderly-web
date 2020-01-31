import { mount } from '@vue/test-utils'
import VueBootstrapTypeaheadList from '../../../js/components/typeahead/typeaheadList.vue'
import VueBootstrapTypeaheadListItem from '../../../js/components/typeahead/typeaheadListItem.vue'
import Vue from "vue";

describe('VueBootstrapTypeaheadList', () => {
    let wrapper

    const demoData = [
        {
            id: 0,
            data: 'Canada',
            text: 'Canada'
        },
        {
            id: 1,
            data: 'USA',
            text: 'USA'
        },
        {
            id: 2,
            data: 'Mexico',
            text: 'Mexico'
        },
        {
            id: 3,
            data: 'Canadiana',
            text: 'Canadiana'
        }
    ]

    beforeEach(() => {
        wrapper = mount(VueBootstrapTypeaheadList, {
            propsData: {
                data: demoData
            }
        })
    })

    it('Mounts and renders a list-group div', () => {
        expect(wrapper.is('div')).toBe(true)
        expect(wrapper.classes()).toContain('list-group')
    })

    it('Matches items when there is a query', async () => {
        expect(wrapper.vm.matchedItems.length).toBe(0)
        wrapper.setProps({
            query: 'Can'
        })
        await Vue.nextTick();
        expect(wrapper.vm.matchedItems.length).toBe(2)
        expect(wrapper.findAll(VueBootstrapTypeaheadListItem).length).toBe(2)
        wrapper.setProps({
            query: 'Canada'
        })
        await Vue.nextTick();
        expect(wrapper.vm.matchedItems.length).toBe(1)
        expect(wrapper.findAll(VueBootstrapTypeaheadListItem).length).toBe(1)
    })

    it('Limits the number of matches with maxMatches', async () => {
        wrapper.setProps({
            query: 'can'
        })
        await Vue.nextTick();
        expect(wrapper.vm.matchedItems.length).toBe(2)
        wrapper.setProps({
            maxMatches: 1
        })
        await Vue.nextTick();
        expect(wrapper.vm.matchedItems.length).toBe(1)
    })

    it('Uses minMatchingChars to filter the number of matches', async () => {
        wrapper.setProps({
            query: 'c',
            minMatchingChars: 1
        })
        await Vue.nextTick();
        expect(wrapper.findAll(VueBootstrapTypeaheadListItem).length).toBe(3)
    })

    it('Highlights text matches properly', async () => {
        wrapper.setProps({
            query: 'Canada'
        })
        await Vue.nextTick();
        expect(wrapper.find(VueBootstrapTypeaheadListItem).vm.htmlText).toBe('<strong>Canada</strong>')
    })

    it('Resets the active list item', async () => {
        wrapper.setProps({
            query: 'Can'
        })
        await Vue.nextTick();
        wrapper.vm.$parent.$emit('keyup.down')
        wrapper.vm.resetActiveListItem()
        expect(wrapper.vm.activeListItem).toBe(-1)
    })

    it('Selects next list item on keyup.down', async () => {
        wrapper.setProps({
            query: 'Can'
        })
        await Vue.nextTick();
        wrapper.vm.$parent.$emit('keyup.down')
        expect(wrapper.vm.activeListItem).toBe(0)
    })

    it('Wraps back to input on keyup.down at bottom of list', async () => {
        wrapper.setProps({
            query: 'Canada'
        })
        await Vue.nextTick();
        wrapper.vm.$parent.$emit('keyup.down')
        wrapper.vm.$parent.$emit('keyup.down')
        expect(wrapper.vm.activeListItem).toBe(-1)
    })

    it('Selects previous list item on keyup.up', async () => {
        wrapper.setProps({
            query: 'Can'
        })
        await Vue.nextTick();
        wrapper.vm.$parent.$emit('keyup.up')
        expect(wrapper.vm.activeListItem).toBe(wrapper.vm.matchedItems.length - 1)
    })

    it('Selects input on keyup.up at when at top of the list', async () => {
        wrapper.setProps({
            query: 'Can'
        })
        await Vue.nextTick();
        wrapper.vm.$parent.$emit('keyup.down')
        wrapper.vm.$parent.$emit('keyup.up')
        expect(wrapper.vm.activeListItem).toBe(-1)
    })

    it('Hits active item on keyup.enter', async (done) => {
        wrapper.setProps({
            query: 'Can'
        })
        await Vue.nextTick();
        wrapper.vm.$parent.$emit('keyup.down') // advance active Item
        await Vue.nextTick();
        wrapper.vm.$on('hit', (hitItem) => {
            expect(hitItem).toBe(wrapper.vm.matchedItems[0])
            done()
        })

        wrapper.vm.$parent.$emit('keyup.enter')
    })

    it('Indicates list item is active or inactive', async () => {
        wrapper.setProps({
            query: 'Can'
        })
        await Vue.nextTick();
        wrapper.vm.$parent.$emit('keyup.down')
        await Vue.nextTick();
        expect(wrapper.vm.isListItemActive(0)).toBeTruthy()
        expect(wrapper.vm.isListItemActive(1)).toBeFalsy()
    })

})