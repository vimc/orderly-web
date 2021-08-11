import {mount} from "@vue/test-utils";
import Vue from "vue";
import ChangeLog from "../../../js/components/runReport/changeLog.vue";

describe(`changeLog`, () => {
    const changelogTypeOptions = ["internal", "public"]

    const getWrapper = (propsData: any = {}) => {
        return mount(ChangeLog,
            {
                propsData: {
                    changelogTypeOptions: changelogTypeOptions,
                    customStyle: {label: "col-sm-2 text-right", control: "col-sm-6"},
                    ...propsData,
                }
            })
    }

    it(`renders component as expected`, () => {
        const wrapper = getWrapper();
        expect(wrapper.find("#changelog-message").exists()).toBeTruthy()
        expect(wrapper.find("#changelog-type").exists()).toBeTruthy()
        expect(wrapper.vm.$data).toEqual({"changeLogMessageValue": "", "changeLogTypeValue": "internal"})
        expect(wrapper.vm.$props).toEqual(
            {
                changelogTypeOptions,
                customStyle: {label: "col-sm-2 text-right", control: "col-sm-6"}
            })
    })

    it(`emits null changelog when component gets mounted`, () => {
        const wrapper = getWrapper();
        expect(wrapper.emitted().changelog.length).toBe(1)
        expect(wrapper.emitted().changelog[0][0]).toBe(null)
    })

    it(`can set and get changelog message`, async () => {
        const wrapper = getWrapper();
        const message = wrapper.find("#changelogMessage")
        await message.setValue("new message")
        expect(wrapper.vm.$data.changeLogMessageValue).toBe("new message")

        expect(wrapper.emitted().changelog.length).toBe(2)
        expect(wrapper.emitted().changelog[1][0].message).toEqual("new message")

        const messageValue = message.element as HTMLTextAreaElement
        expect(messageValue.value).toBe("new message")
    })

    it(`can select and get changelog type`, async () => {
        const wrapper = getWrapper();
        await wrapper.find("#changelogMessage").setValue("new message")

        wrapper.find("#changelogType").findAll("option").at(1).setSelected()
        expect(wrapper.emitted().changelog.length).toBe(3)
        expect(wrapper.emitted().changelog[0][0]).toBeNull() // on mount
        expect(wrapper.emitted().changelog[1][0]["type"]).toEqual(changelogTypeOptions[0]) // message set
        expect(wrapper.emitted().changelog[2][0]["type"]).toEqual(changelogTypeOptions[1]) // type updated

        expect(wrapper.vm.$data.changeLogTypeValue).toBe("public")
        const typeValue = wrapper.find("#changelogType").element as HTMLSelectElement
        expect(typeValue.value).toBe("public")
    })

    it(`renders changelog col styles correctly if custom-style prop is set`, () => {
        const wrapper = getWrapper();

        const label = ["col-form-label", "col-sm-2", "text-right"]
        const control = ["col-sm-6"]

        const changelogMessage = wrapper.find(ChangeLog).find("#changelog-message")
        const changelogType= wrapper.find(ChangeLog).find("#changelog-type")

        expect(changelogMessage.find("label").classes()).toEqual(label)
        expect(changelogMessage.find("#change-message-control").classes()).toEqual(control)
        expect(changelogType.find("label").classes()).toEqual(label)
        expect(changelogType.find("#change-type-control").classes()).toEqual(control)
    });

    it(`can set changelog message from initial value`, async () => {
        const wrapper = getWrapper({initialMessage: "some message"});
        expect(wrapper.vm.$data.changeLogMessageValue).toBe("some message");
        await Vue.nextTick();
        expect((wrapper.find("#changelogMessage").element as HTMLTextAreaElement).value).toBe("some message");
    });

    it(`can set changelog message from initial type`, async () => {
        const wrapper = getWrapper({initialType: "public"});
        expect(wrapper.vm.$data.changeLogTypeValue).toBe("public");
        await Vue.nextTick();
        expect((wrapper.find("#changelogType").element as HTMLSelectElement).value).toBe("public");
        expect(wrapper.emitted().changelogType).toBeUndefined();
    });
})
